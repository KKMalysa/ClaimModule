package com.kkmalysa.claimmodule.domain.events;

import com.kkmalysa.claimmodule.application.submission.ClaimSubmission;
import com.kkmalysa.claimmodule.domain.claim.Claim;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class DomainEventBuilder {

    private DomainEventBuilder() {}

    public static ClaimSubmittedStep claimSubmitted() {
        return new BuilderImpl(Clock.systemUTC());
    }

    static ClaimSubmittedStep claimSubmitted(Clock clock) { // test-friendly
        return new BuilderImpl(clock);
    }

    // --- steps ---
    public interface ClaimSubmittedStep {
        ClaimStep claim(Claim claim);
    }

    public interface ClaimStep {
        SubmissionStep submission(ClaimSubmission submission);
    }

    public interface SubmissionStep {
        ActorStep actor(String actor);
    }

    public interface ActorStep {
        DomainEvent build();
    }

    private static final class BuilderImpl
            implements ClaimSubmittedStep, ClaimStep, SubmissionStep, ActorStep {

        private final Clock clock;

        private Claim claim;
        private ClaimSubmission submission;
        private String actor;

        private BuilderImpl(Clock clock) {
            this.clock = Objects.requireNonNull(clock);
        }

        @Override
        public ClaimStep claim(Claim claim) {
            this.claim = Objects.requireNonNull(claim);
            return this;
        }

        @Override
        public SubmissionStep submission(ClaimSubmission submission) {
            this.submission = Objects.requireNonNull(submission);
            return this;
        }

        @Override
        public ActorStep actor(String actor) {
            this.actor = requireText(actor, "actor");
            return this;
        }

        @Override
        public DomainEvent build() {
            return new ClaimSubmittedEvent(
                    claim.getClaimId(),
                    submission.getSubmissionId(),
                    submission.getChannel().name(),
                    actor,
                    Instant.now(clock)
            );
        }

        private static String requireText(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
            return value;
        }
    }
}
