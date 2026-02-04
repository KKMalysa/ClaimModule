package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.adapters.out.attachments.FakeAttachmentProvider;
import com.kkmalysa.claimmodule.adapters.out.persistence.InMemoryClaimRepository;
import com.kkmalysa.claimmodule.adapters.out.policy.FakePolicyProvider;
import com.kkmalysa.claimmodule.adapters.out.submission.InMemorySubmissionGateway;
import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.claim.ClaimBuilder;
import com.kkmalysa.claimmodule.domain.claim.ClaimStatus;
import com.kkmalysa.claimmodule.domain.incident.Incident;
import com.kkmalysa.claimmodule.domain.incident.IncidentBuilder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SubmitClaimUseCaseTest {

    private Claim createAndStoreClaim(InMemoryClaimRepository repo, String claimId, String policyId) {
        Incident incident = IncidentBuilder.theft()
                .occurredAt(Instant.parse("2026-02-01T10:00:00Z"))
                .location("Warsaw")
                .forTheft()
                .stolenItem("Laptop")
                .policeReportNumber("KRP-123/2026")
                .build();

        Claim claim = ClaimBuilder.fnol()
                .policyId(policyId)
                .incident(incident)
                .createdBy("user-1")
                .channel("MANUAL")
                .claimId(claimId)
                .createdAt(Instant.parse("2026-02-01T10:05:00Z"))
                .build();

        repo.save(claim);
        return claim;
    }

    @Test
    void submit_auto_sendsSubmission_and_updatesClaimToSubmitted() {
        InMemoryClaimRepository claimRepo = new InMemoryClaimRepository();
        FakePolicyProvider policyProvider = new FakePolicyProvider();
        FakeAttachmentProvider attachmentProvider = new FakeAttachmentProvider();
        InMemorySubmissionGateway gateway = new InMemorySubmissionGateway();

        Claim claim = createAndStoreClaim(claimRepo, "CLM-1", "POL-1");
        attachmentProvider.put(claim.getClaimId(), List.of("att-1", "att-2"));

        SubmitClaimUseCase useCase = new SubmitClaimUseCase(
                claimRepo, policyProvider, attachmentProvider, gateway
        );

        ClaimSubmission submission = useCase.submit(
                claim.getClaimId(),
                SubmissionChannel.AUTO,
                "user-1",
                true
        ).getSubmission();

        // 1) gateway got the submission
        assertThat(gateway.sentSubmissions()).hasSize(1);
        assertThat(submission.getChannel()).isEqualTo(SubmissionChannel.AUTO);
        assertThat(submission.getAttachmentIds()).hasSize(2);
        assertThat(submission.isTermsAccepted()).isTrue();

        // 2) claim status updated & saved
        Claim updated = claimRepo.getById(claim.getClaimId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.SUBMITTED);
    }

    @Test
    void submit_manual_sendsSubmission_and_keepsClaimDraft() {
        InMemoryClaimRepository claimRepo = new InMemoryClaimRepository();
        FakePolicyProvider policyProvider = new FakePolicyProvider();
        FakeAttachmentProvider attachmentProvider = new FakeAttachmentProvider();
        InMemorySubmissionGateway gateway = new InMemorySubmissionGateway();

        Claim claim = createAndStoreClaim(claimRepo, "CLM-2", "POL-2");
        attachmentProvider.put(claim.getClaimId(), List.of()); // manual can be empty

        SubmitClaimUseCase useCase = new SubmitClaimUseCase(
                claimRepo, policyProvider, attachmentProvider, gateway
        );

        ClaimSubmission submission = useCase.submit(
                claim.getClaimId(),
                SubmissionChannel.MANUAL,
                "agent-7",
                false
        ).getSubmission();

        assertThat(gateway.sentSubmissions()).hasSize(1);
        assertThat(submission.getChannel()).isEqualTo(SubmissionChannel.MANUAL);
        assertThat(submission.getAttachmentIds()).isEmpty();

        Claim updated = claimRepo.getById(claim.getClaimId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.DRAFT);
    }
}
