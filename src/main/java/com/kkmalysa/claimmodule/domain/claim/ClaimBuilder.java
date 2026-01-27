package com.kkmalysa.claimmodule.domain.claim;

import com.kkmalysa.claimmodule.domain.incident.Incident;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Builder #2 - ClaimBuilder (FNOL = First Notice of Loss).
 *
 * fnol() -> PolicyStep -> IncidentStep -> ReporterStep -> ChannelStep -> OptionalStep -> build()
 *
 * This is a Process/Workflow Builder: it guides the creation of a Claim through the FNOL flow,
 * combining required domain inputs (policyId + Incident) and request context (createdBy + channel).
 *
 * Step Builder is used here to enforce the build order and mandatory data at compile time
 * (PolicyStep -> IncidentStep -> ReporterStep -> ChannelStep -> build()).
 * The BuilderImpl implements all step interfaces and returns "this" cast to the next interface.
 *
 * In build() we apply FNOL defaults/business rules:
 * - generate claimId and createdAt (Clock injected for testability),
 * - derive initial ClaimStatus from channel (e.g. MANUAL -> DRAFT, AUTO -> SUBMITTED).
 *
 * Future hook: this is the natural place (or the surrounding use-case) to emit ClaimCreatedEvent.
 */

public final class ClaimBuilder {

    private ClaimBuilder() {}

    // entrypoint “use-case’owy”
    public static PolicyStep fnol() {
        return new BuilderImpl(Clock.systemUTC());
    }

    // entrypoint for tests
    static PolicyStep fnol(Clock clock) {
        return new BuilderImpl(clock);
    }

    // --- STEPS (sequence) ---
    public interface PolicyStep {
        IncidentStep policyId(String policyId);
    }

    public interface IncidentStep {
        ReporterStep incident(Incident incident);
    }

    public interface ReporterStep {
        ChannelStep createdBy(String actor);
    }

    public interface ChannelStep {
        OptionalStep channel(String channel); // "MANUAL"/"AUTO" (or enum later)
    }

    public interface OptionalStep {
        OptionalStep claimId(String claimId);     // override for tests / import
        OptionalStep createdAt(Instant createdAt);// override for tests
        Claim build();
    }

    // --- IMPLEMENTATION ---
    private static final class BuilderImpl
            implements PolicyStep, IncidentStep, ReporterStep, ChannelStep, OptionalStep {

        private final Clock clock;

        private String claimId;
        private String policyId;
        private Incident incident;

        private String createdBy;
        private String channel;

        private Instant createdAt;

        private BuilderImpl(Clock clock) {
            this.clock = Objects.requireNonNull(clock);
        }

        @Override
        public IncidentStep policyId(String policyId) {
            this.policyId = requireText(policyId, "policyId");
            return this;
        }

        @Override
        public ReporterStep incident(Incident incident) {
            this.incident = Objects.requireNonNull(incident, "incident");
            return this;
        }

        @Override
        public ChannelStep createdBy(String actor) {
            this.createdBy = requireText(actor, "createdBy");
            return this;
        }

        @Override
        public OptionalStep channel(String channel) {
            this.channel = requireText(channel, "channel");
            return this;
        }

        @Override
        public OptionalStep claimId(String claimId) {
            this.claimId = requireText(claimId, "claimId");
            return this;
        }

        @Override
        public OptionalStep createdAt(Instant createdAt) {
            this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
            return this;
        }

        @Override
        public Claim build() {
            // the required steps are dictated by the interface types,
            // but we still validate (e.g., someone reflected on null/blank)
            if (policyId == null) throw new IllegalStateException("policyId is required");
            if (incident == null) throw new IllegalStateException("incident is required");
            if (createdBy == null) throw new IllegalStateException("createdBy is required");
            if (channel == null) throw new IllegalStateException("channel is required");

            String id = (claimId != null) ? claimId : UUID.randomUUID().toString();
            Instant now = (createdAt != null) ? createdAt : Instant.now(clock);

            ClaimStatus initialStatus = initialStatusFor(channel);

            return new Claim(
                    id,
                    policyId,
                    incident,
                    initialStatus,
                    now,
                    createdBy,
                    channel
            );
        }

        private static ClaimStatus initialStatusFor(String channel) {
            return switch (channel) {
                case "MANUAL" -> ClaimStatus.DRAFT;
                case "AUTO" -> ClaimStatus.SUBMITTED;
                default -> throw new IllegalArgumentException("Unknown channel: " + channel);
            };
        }

        private static String requireText(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
            return value;
        }
    }
}

