package com.kkmalysa.claimmodule.domain.claim;

import com.kkmalysa.claimmodule.domain.incident.Incident;

import java.time.Instant;
import java.util.Objects;

public final class Claim {
    private final String claimId;
    private final String policyId;
    private final Incident incident;

    private final ClaimStatus status;
    private final Instant createdAt;

    private final String createdBy;   // actor (np. userId / system)
    private final String channel;     // how did the client reported a claim. "via phone agent / via app/portal". in future type would be changed.

    Claim(String claimId, String policyId, Incident incident, ClaimStatus status, Instant createdAt, String createdBy, String channel) {
        this.claimId = Objects.requireNonNull(claimId);
        this.policyId = Objects.requireNonNull(policyId);
        this.incident = Objects.requireNonNull(incident);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.createdBy = Objects.requireNonNull(createdBy);
        this.channel = Objects.requireNonNull(channel);
    }

    public String getClaimId() { return claimId; }
    public String getPolicyId() { return policyId; }
    public Incident getIncident() { return incident; }
    public ClaimStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getChannel() { return channel; }

    // simple for now: status change can return new Claim (immutable), or create events in future.
    public Claim withStatus(ClaimStatus newStatus) {
        return new Claim(claimId, policyId, incident, newStatus, createdAt, createdBy, channel);
    }
}

