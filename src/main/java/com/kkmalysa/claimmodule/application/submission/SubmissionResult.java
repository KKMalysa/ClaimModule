package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.events.DomainEvent;

import java.util.Objects;

public final class SubmissionResult {

    private final ClaimSubmission submission;
    private final Claim updatedClaim;
    private final DomainEvent event;

    public SubmissionResult(ClaimSubmission submission, Claim updatedClaim, DomainEvent event) {
        this.submission = Objects.requireNonNull(submission);
        this.updatedClaim = Objects.requireNonNull(updatedClaim);
        this.event = Objects.requireNonNull(event);
    }

    public ClaimSubmission getSubmission() {
        return submission;
    }

    public Claim getUpdatedClaim() {
        return updatedClaim;
    }

    public DomainEvent getEvent() {
        return event;
    }
}
