package com.kkmalysa.claimmodule.application.submission;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * This is not a domain, not an entity, not JPA.
 * This is a snapshot of data, a ready payload to send.
 * True at the submission moment.
 * That's why there is no setters, no logic, and everything is in constructor
 */
public final class ClaimSubmission {

    private final String submissionId;
    private final Instant submittedAt;

    private final SubmissionChannel channel;
    private final String submittedBy;

    // claim/policy context
    private final String claimId;
    private final String policyId;
    private final String policyNumber;

    // incident summary (minimum, without an entity )
    private final String incidentType;
    private final Instant occurredAt;
    private final String location;

    // attachments / declarations
    private final List<String> attachmentIds;
    private final boolean termsAccepted;

    ClaimSubmission(
            String submissionId,
            Instant submittedAt,
            SubmissionChannel channel,
            String submittedBy,
            String claimId,
            String policyId,
            String policyNumber,
            String incidentType,
            Instant occurredAt,
            String location,
            List<String> attachmentIds,
            boolean termsAccepted
    ) {
        this.submissionId = Objects.requireNonNull(submissionId);
        this.submittedAt = Objects.requireNonNull(submittedAt);
        this.channel = Objects.requireNonNull(channel);
        this.submittedBy = Objects.requireNonNull(submittedBy);

        this.claimId = Objects.requireNonNull(claimId);
        this.policyId = Objects.requireNonNull(policyId);
        this.policyNumber = Objects.requireNonNull(policyNumber);

        this.incidentType = Objects.requireNonNull(incidentType);
        this.occurredAt = Objects.requireNonNull(occurredAt);
        this.location = Objects.requireNonNull(location);

        this.attachmentIds = List.copyOf(Objects.requireNonNull(attachmentIds));
        this.termsAccepted = termsAccepted;
    }

    public String getSubmissionId() { return submissionId; }
    public Instant getSubmittedAt() { return submittedAt; }
    public SubmissionChannel getChannel() { return channel; }
    public String getSubmittedBy() { return submittedBy; }

    public String getClaimId() { return claimId; }
    public String getPolicyId() { return policyId; }
    public String getPolicyNumber() { return policyNumber; }

    public String getIncidentType() { return incidentType; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getLocation() { return location; }

    public List<String> getAttachmentIds() { return attachmentIds; }
    public boolean isTermsAccepted() { return termsAccepted; }
}

