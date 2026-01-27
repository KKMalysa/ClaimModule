package com.kkmalysa.claimmodule.domain.claim;

import com.kkmalysa.claimmodule.domain.incident.Incident;
import com.kkmalysa.claimmodule.domain.incident.IncidentBuilder;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ClaimBuilderTest {

    private static Incident sampleIncidentAuto() {
        return IncidentBuilder.auto()
                .occurredAt(Instant.parse("2026-01-01T10:00:00Z"))
                .location("Warsaw")
                .description("Crash")
                .forAuto()
                .vin("WVWZZZ1JZXW000001")
                .licensePlate("WX12345")
                .build();
    }

    @Test
    void shouldBuildClaimWithGeneratedIdAndCreatedAt_fromClock_andStatusDraftForManual() {
        // given
        Instant fixedNow = Instant.parse("2026-01-27T12:00:00Z");
        Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        Incident incident = sampleIncidentAuto();

        // when
        Claim claim = ClaimBuilder.fnol(clock)
                .policyId("POL-001")
                .incident(incident)
                .createdBy("agent-1")
                .channel("MANUAL")
                .build();

        // then
        assertNotNull(claim);
        assertNotNull(claim.getClaimId());
        assertFalse(claim.getClaimId().isBlank());

        assertEquals("POL-001", claim.getPolicyId());
        assertSame(incident, claim.getIncident());

        assertEquals(ClaimStatus.DRAFT, claim.getStatus());
        assertEquals(fixedNow, claim.getCreatedAt());

        assertEquals("agent-1", claim.getCreatedBy());
        assertEquals("MANUAL", claim.getChannel());
    }

    @Test
    void shouldSetSubmittedForAutoChannel() {
        Instant fixedNow = Instant.parse("2026-01-27T12:00:00Z");
        Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        Claim claim = ClaimBuilder.fnol(clock)
                .policyId("POL-002")
                .incident(sampleIncidentAuto())
                .createdBy("system")
                .channel("AUTO")
                .build();

        assertEquals(ClaimStatus.SUBMITTED, claim.getStatus());
    }

    @Test
    void shouldAllowOverridingClaimIdAndCreatedAt() {
        Instant fixedNow = Instant.parse("2026-01-27T12:00:00Z");
        Clock clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        Instant customCreatedAt = Instant.parse("2025-12-31T23:59:59Z");

        Claim claim = ClaimBuilder.fnol(clock)
                .policyId("POL-003")
                .incident(sampleIncidentAuto())
                .createdBy("agent-2")
                .channel("MANUAL")
                .claimId("CLM-123")
                .createdAt(customCreatedAt)
                .build();

        assertEquals("CLM-123", claim.getClaimId());
        assertEquals(customCreatedAt, claim.getCreatedAt());
        assertEquals(ClaimStatus.DRAFT, claim.getStatus());
    }

    @Test
    void shouldThrowForUnknownChannel() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-004")
                        .incident(sampleIncidentAuto())
                        .createdBy("agent-3")
                        .channel("EMAIL") // unknown
                        .build()
        );

        assertEquals("Unknown channel: EMAIL", ex.getMessage());
    }

    @Test
    void shouldRejectBlankPolicyId() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("   ")
        );

        assertEquals("policyId must not be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullIncident() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-005")
                        .incident(null)
        );

        // message zależy od JDK, ale u Ciebie ustawiasz ją jawnie
        assertEquals("incident", ex.getMessage());
    }

    @Test
    void shouldRejectBlankCreatedBy() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-006")
                        .incident(sampleIncidentAuto())
                        .createdBy("   ")
        );

        assertEquals("createdBy must not be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankChannel() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-007")
                        .incident(sampleIncidentAuto())
                        .createdBy("agent-4")
                        .channel("   ")
        );

        assertEquals("channel must not be blank", ex.getMessage());
    }

    @Test
    void shouldRejectBlankOverriddenClaimId() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-008")
                        .incident(sampleIncidentAuto())
                        .createdBy("agent-5")
                        .channel("MANUAL")
                        .claimId("  ")
        );

        assertEquals("claimId must not be blank", ex.getMessage());
    }

    @Test
    void shouldRejectNullOverriddenCreatedAt() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-27T12:00:00Z"), ZoneOffset.UTC);

        NullPointerException ex = assertThrows(NullPointerException.class, () ->
                ClaimBuilder.fnol(clock)
                        .policyId("POL-009")
                        .incident(sampleIncidentAuto())
                        .createdBy("agent-6")
                        .channel("MANUAL")
                        .createdAt(null)
        );

        assertEquals("createdAt", ex.getMessage());
    }
}

