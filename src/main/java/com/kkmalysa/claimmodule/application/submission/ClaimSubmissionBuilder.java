package com.kkmalysa.claimmodule.application.submission;

import com.kkmalysa.claimmodule.domain.claim.Claim;
import com.kkmalysa.claimmodule.domain.incident.Incident;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ClaimSubmissionBuilder {

    private ClaimSubmissionBuilder() {}

    public static FromClaimStep create() {
        return new BuilderImpl(Clock.systemUTC());
    }

    static FromClaimStep create(Clock clock) { // test-friendly
        return new BuilderImpl(clock);
    }

    // --- steps (common) ---
    public interface FromClaimStep {
        PolicyStep fromClaim(Claim claim);
    }

    public interface PolicyStep {
        AttachmentsStep policyNumber(String policyNumber);
    }

    public interface AttachmentsStep {
        SubmittedByStep attachments(List<String> attachmentIds);
    }

    public interface SubmittedByStep {
        ChannelSelectStep submittedBy(String actor);
    }

    // --- variant switch (decision point) ---
    public interface ChannelSelectStep {
        ManualStep manual();
        AutoStep auto();
    }

    // --- MANUAL: relaxed rules ---
    public interface ManualStep {
        OptionalStep termsAcceptedOptional(boolean accepted); // opcjonalne
        ClaimSubmission build(); // można build bez terms
    }

    // --- AUTO: strict rules ---
    public interface AutoStep {
        AutoStep termsAccepted(boolean accepted); // wymagane true (sprawdzimy w build)
        ClaimSubmission build();
    }

    // --- common optional overrides ---
    public interface OptionalStep {
        OptionalStep submissionId(String id);
        OptionalStep submittedAt(Instant at);
        ClaimSubmission build();
    }

    private static final class BuilderImpl
            implements FromClaimStep, PolicyStep, AttachmentsStep, SubmittedByStep,
            ChannelSelectStep, ManualStep, AutoStep, OptionalStep {

        private final Clock clock;

        private Claim claim;
        private String policyNumber;
        private List<String> attachmentIds = List.of();
        private String submittedBy;

        private SubmissionChannel channel;
        private boolean termsAccepted;

        private String submissionId;
        private Instant submittedAt;

        private BuilderImpl(Clock clock) {
            this.clock = Objects.requireNonNull(clock);
        }

        @Override
        public PolicyStep fromClaim(Claim claim) {
            this.claim = Objects.requireNonNull(claim, "claim");
            return this;
        }

        @Override
        public AttachmentsStep policyNumber(String policyNumber) {
            this.policyNumber = requireText(policyNumber, "policyNumber");
            return this;
        }

        @Override
        public SubmittedByStep attachments(List<String> attachmentIds) {
            this.attachmentIds = List.copyOf(Objects.requireNonNull(attachmentIds, "attachmentIds"));
            return this;
        }

        @Override
        public ChannelSelectStep submittedBy(String actor) {
            this.submittedBy = requireText(actor, "submittedBy");
            return this;
        }

        @Override
        public ManualStep manual() {
            this.channel = SubmissionChannel.MANUAL;
            return this;
        }

        @Override
        public AutoStep auto() {
            this.channel = SubmissionChannel.AUTO;
            return this;
        }

        @Override
        public OptionalStep termsAcceptedOptional(boolean accepted) {
            this.termsAccepted = accepted;
            return this;
        }

        @Override
        public AutoStep termsAccepted(boolean accepted) {
            this.termsAccepted = accepted;
            return this;
        }

        @Override
        public OptionalStep submissionId(String id) {
            this.submissionId = requireText(id, "submissionId");
            return this;
        }

        @Override
        public OptionalStep submittedAt(Instant at) {
            this.submittedAt = Objects.requireNonNull(at, "submittedAt");
            return this;
        }

        @Override
        public ClaimSubmission build() {
            // required common
            if (claim == null) throw new IllegalStateException("claim is required");
            if (policyNumber == null) throw new IllegalStateException("policyNumber is required");
            if (submittedBy == null) throw new IllegalStateException("submittedBy is required");
            if (channel == null) throw new IllegalStateException("channel is required");

            // channel-specific rules
            if (channel == SubmissionChannel.AUTO) {
                if (!termsAccepted) throw new IllegalStateException("termsAccepted must be true for AUTO channel");
                if (attachmentIds.isEmpty()) throw new IllegalStateException("at least 1 attachment is required for AUTO channel");
            }

            String id = (submissionId != null) ? submissionId : UUID.randomUUID().toString();
            Instant at = (submittedAt != null) ? submittedAt : Instant.now(clock);

            Incident incident = claim.getIncident();

            // HERE FIX #2
            String incidentType = incident.getIncidentType().name();

            return new ClaimSubmission(
                    id,
                    at,
                    channel,
                    submittedBy,
                    claim.getClaimId(),
                    claim.getPolicyId(),
                    policyNumber,
                    incidentType,
                    incident.getOccurredAt(),
                    incident.getLocation(),
                    attachmentIds,
                    termsAccepted
            );
        }

        private static String requireText(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
            return value;
        }
    }
}
