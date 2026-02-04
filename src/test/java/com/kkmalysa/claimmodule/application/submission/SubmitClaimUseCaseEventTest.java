package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.adapters.out.attachments.FakeAttachmentProvider;
import com.kkmalysa.claimmodule.adapters.out.persistence.InMemoryClaimRepository;
import com.kkmalysa.claimmodule.adapters.out.policy.FakePolicyProvider;
import com.kkmalysa.claimmodule.adapters.out.submission.InMemorySubmissionGateway;
import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.claim.ClaimBuilder;
import com.kkmalysa.claimmodule.domain.claim.ClaimStatus;
import com.kkmalysa.claimmodule.domain.events.ClaimSubmittedEvent;
import com.kkmalysa.claimmodule.domain.events.DomainEvent;
import com.kkmalysa.claimmodule.domain.incident.Incident;
import com.kkmalysa.claimmodule.domain.incident.IncidentBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SubmitClaimUseCaseEventTest {

    private Claim createAndStoreClaim(InMemoryClaimRepository repo, String claimId, String policyId) {
        Incident incident = IncidentBuilder.auto()
                .occurredAt(Instant.parse("2026-02-01T10:00:00Z"))
                .location("Warsaw")
                .forAuto()
                .vin("WVWZZZ1JZXW000001")
                .licensePlate("WI1234A")
                .build();

        Claim claim = ClaimBuilder.fnol()
                .policyId(policyId)
                .incident(incident)
                .createdBy("user-1")
                .channel("AUTO")
                .claimId(claimId)
                .createdAt(Instant.parse("2026-02-01T10:05:00Z"))
                .build();

        repo.save(claim);
        return claim;
    }

    @Test
    void submit_auto_returnsSubmissionResult_withEvent_andUpdatedClaim() {
        // adapters
        InMemoryClaimRepository claimRepo = new InMemoryClaimRepository();
        FakePolicyProvider policyProvider = new FakePolicyProvider();
        FakeAttachmentProvider attachmentProvider = new FakeAttachmentProvider();
        InMemorySubmissionGateway gateway = new InMemorySubmissionGateway();

        // data
        Claim claim = createAndStoreClaim(claimRepo, "CLM-1", "POL-1");
        attachmentProvider.put(claim.getClaimId(), List.of("att-1"));

        SubmitClaimUseCase useCase = new SubmitClaimUseCase(
                claimRepo, policyProvider, attachmentProvider, gateway
        );

        // act
        SubmissionResult result = useCase.submit(
                claim.getClaimId(),
                SubmissionChannel.AUTO,
                "user-1",
                true
        );

        // assert: result is complete
        assertThat(result).isNotNull();
        assertThat(result.getSubmission()).isNotNull();
        assertThat(result.getUpdatedClaim()).isNotNull();
        assertThat(result.getEvent()).isNotNull();

        // assert: gateway was used (submission sent)
        assertThat(gateway.sentSubmissions()).hasSize(1);

        // assert: claim status updated and saved
        Claim saved = claimRepo.getById(claim.getClaimId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
        assertThat(result.getUpdatedClaim().getStatus()).isEqualTo(ClaimStatus.SUBMITTED);

        // assert: event correctness
        DomainEvent event = result.getEvent();
        assertThat(event).isInstanceOf(ClaimSubmittedEvent.class);

        ClaimSubmittedEvent submittedEvent = (ClaimSubmittedEvent) event;
        assertThat(submittedEvent.getClaimId()).isEqualTo(claim.getClaimId());
        assertThat(submittedEvent.getSubmissionId()).isEqualTo(result.getSubmission().getSubmissionId());
        assertThat(submittedEvent.getChannel()).isEqualTo("AUTO");
        assertThat(submittedEvent.getActor()).isEqualTo("user-1");
        assertThat(submittedEvent.occurredAt()).isNotNull();
    }

    @Test
    void submit_auto_fails_whenTermsNotAccepted_andDoesNotSendAnything() {
        InMemoryClaimRepository claimRepo = new InMemoryClaimRepository();
        FakePolicyProvider policyProvider = new FakePolicyProvider();
        FakeAttachmentProvider attachmentProvider = new FakeAttachmentProvider();
        InMemorySubmissionGateway gateway = new InMemorySubmissionGateway();

        Claim claim = createAndStoreClaim(claimRepo, "CLM-2", "POL-2");
        attachmentProvider.put(claim.getClaimId(), List.of("att-1"));

        SubmitClaimUseCase useCase = new SubmitClaimUseCase(
                claimRepo, policyProvider, attachmentProvider, gateway
        );

        assertThatThrownBy(() -> useCase.submit(
                claim.getClaimId(),
                SubmissionChannel.AUTO,
                "user-1",
                false
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("termsAccepted must be true");

        // nothing was sent
        assertThat(gateway.sentSubmissions()).isEmpty();

        // claim wasn't updated
        Claim saved = claimRepo.getById(claim.getClaimId()).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(claim.getStatus());
    }
}
