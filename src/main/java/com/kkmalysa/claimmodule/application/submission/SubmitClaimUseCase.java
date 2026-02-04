package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.claim.ClaimStatus;
import com.kkmalysa.claimmodule.domain.events.DomainEvent;
import com.kkmalysa.claimmodule.domain.events.DomainEventBuilder;
import com.kkmalysa.claimmodule.ports.out.AttachmentProvider;
import com.kkmalysa.claimmodule.ports.out.ClaimRepository;
import com.kkmalysa.claimmodule.ports.out.PolicyProvider;
import com.kkmalysa.claimmodule.ports.out.SubmissionGateway;

import java.util.List;
import java.util.Objects;

public final class SubmitClaimUseCase {

    private final ClaimRepository claimRepository;
    private final PolicyProvider policyProvider;
    private final AttachmentProvider attachmentProvider;
    private final SubmissionGateway submissionGateway;

    public SubmitClaimUseCase(
            ClaimRepository claimRepository,
            PolicyProvider policyProvider,
            AttachmentProvider attachmentProvider,
            SubmissionGateway submissionGateway
    ) {
        this.claimRepository = Objects.requireNonNull(claimRepository);
        this.policyProvider = Objects.requireNonNull(policyProvider);
        this.attachmentProvider = Objects.requireNonNull(attachmentProvider);
        this.submissionGateway = Objects.requireNonNull(submissionGateway);
    }

    public SubmissionResult submit(String claimId, SubmissionChannel channel, String actor, boolean termsAccepted) {
        Claim claim = claimRepository.getById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        String policyNumber = policyProvider.getPolicyNumber(claim.getPolicyId())
                .orElseThrow(() -> new IllegalStateException("Policy number not found for policyId: " + claim.getPolicyId()));

        List<String> attachments = attachmentProvider.findAttachmentIdsForClaim(claimId);

        // ======= (variant switch: MANUAL vs AUTO) =======
        ClaimSubmissionBuilder.ChannelSelectStep base = ClaimSubmissionBuilder.create()
                .fromClaim(claim)
                .policyNumber(policyNumber)
                .attachments(attachments)
                .submittedBy(actor);

        ClaimSubmission submission = switch (channel) {
            case AUTO -> base.auto()
                    .termsAccepted(termsAccepted)
                    .build();
            case MANUAL -> base.manual()
                    .build();
        };
        // ================================================================

        submissionGateway.send(submission);

        ClaimStatus newStatus = (channel == SubmissionChannel.AUTO) ? ClaimStatus.SUBMITTED : ClaimStatus.DRAFT;
        Claim updatedClaim = claim.withStatus(newStatus);
        claimRepository.save(updatedClaim);

        DomainEvent event = DomainEventBuilder
                .claimSubmitted()
                .claim(updatedClaim)
                .submission(submission)
                .actor(actor)
                .build();

        return new SubmissionResult(submission, updatedClaim, event);
    }
}
