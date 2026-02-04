package com.kkmalysa.claimmodule.domain.events;

import java.time.Instant;
import java.util.Objects;

public final class ClaimSubmittedEvent implements DomainEvent {

    private final String claimId;
    private final String submissionId;
    private final String channel;
    private final String actor;
    private final Instant occurredAt;

    public ClaimSubmittedEvent(
            String claimId,
            String submissionId,
            String channel,
            String actor,
            Instant occurredAt
    ) {
        this.claimId = Objects.requireNonNull(claimId);
        this.submissionId = Objects.requireNonNull(submissionId);
        this.channel = Objects.requireNonNull(channel);
        this.actor = Objects.requireNonNull(actor);
        this.occurredAt = Objects.requireNonNull(occurredAt);
    }

    public String getClaimId() {
        return claimId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getChannel() {
        return channel;
    }

    public String getActor() {
        return actor;
    }

    @Override
    public String eventType() {
        return "ClaimSubmitted";
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
