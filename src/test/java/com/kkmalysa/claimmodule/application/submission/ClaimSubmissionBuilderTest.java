package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.claim.ClaimBuilder;
import com.kkmalysa.claimmodule.domain.incident.Incident;
import com.kkmalysa.claimmodule.domain.incident.IncidentBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ClaimSubmissionBuilderTest {

    private Claim sampleClaim() {
        Incident incident = IncidentBuilder.auto()
                .occurredAt(Instant.parse("2026-02-01T10:00:00Z"))
                .location("Warsaw")
                .forAuto()
                .vin("WVWZZZ1JZXW000001")
                .licensePlate("WI1234A")
                .build();

        return ClaimBuilder.fnol()
                .policyId("POL-123")
                .incident(incident)
                .createdBy("user-1")
                .channel("AUTO")
                .claimId("CLM-1")
                .createdAt(Instant.parse("2026-02-01T10:05:00Z"))
                .build();
    }

    @Test
    void autoChannel_requires_termsAccepted_true() {
        Claim claim = sampleClaim();

        ClaimSubmissionBuilder.ChannelSelectStep base = ClaimSubmissionBuilder.create()
                .fromClaim(claim)
                .policyNumber("PN-POL-123")
                .attachments(List.of("att-1"))
                .submittedBy("user-1");

        assertThatThrownBy(() -> base.auto()
                .termsAccepted(false)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("termsAccepted must be true");
    }

    @Test
    void autoChannel_requires_atLeastOneAttachment() {
        Claim claim = sampleClaim();

        ClaimSubmissionBuilder.ChannelSelectStep base = ClaimSubmissionBuilder.create()
                .fromClaim(claim)
                .policyNumber("PN-POL-123")
                .attachments(List.of()) // empty
                .submittedBy("user-1");

        assertThatThrownBy(() -> base.auto()
                .termsAccepted(true)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 1 attachment");
    }

    @Test
    void manualChannel_allows_emptyAttachments_and_termsNotAccepted() {
        Claim claim = sampleClaim();

        ClaimSubmission submission = ClaimSubmissionBuilder.create()
                .fromClaim(claim)
                .policyNumber("PN-POL-123")
                .attachments(List.of()) // empty allowed
                .submittedBy("agent-7")
                .manual()
                .build();

        assertThat(submission.getChannel()).isEqualTo(SubmissionChannel.MANUAL);
        assertThat(submission.getAttachmentIds()).isEmpty();
        assertThat(submission.isTermsAccepted()).isFalse();
    }
}
