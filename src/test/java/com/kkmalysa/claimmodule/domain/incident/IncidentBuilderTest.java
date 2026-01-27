package com.kkmalysa.claimmodule.domain.incident;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IncidentBuilderTest {

    @Test
    void shouldBuildAutoIncident() {
        Instant now = Instant.parse("2026-01-27T10:15:30Z");

        Incident incident = IncidentBuilder.auto()
                .occurredAt(now)
                .location("Warsaw")
                .description("Minor crash")
                .forAuto()
                .vin("WVWZZZ1JZXW000001")
                .licensePlate("WX12345")
                .build();

        assertEquals(IncidentType.AUTO, incident.getIncidentType());
        assertEquals(now, incident.getOccurredAt());
        assertEquals("Warsaw", incident.getLocation());
        assertEquals("Minor crash", incident.getDescription().orElseThrow());
        assertEquals("WVWZZZ1JZXW000001", incident.getVin().orElseThrow());
        assertEquals("WX12345", incident.getLicensePlate().orElseThrow());
    }

    @Test
    void shouldFailWhenOccurredAtMissing() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                IncidentBuilder.auto()
                        .location("Warsaw")
                        .description("Crash")
                        .forAuto()
                        .vin("VIN")
                        .licensePlate("PLATE")
                        .build()
        );

        assertEquals("occurredAt is required", ex.getMessage());
    }

    @Test
    void shouldFailWhenAutoVinMissing() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                IncidentBuilder.auto()
                        .occurredAt(Instant.now())
                        .location("Warsaw")
                        .description("Crash")
                        .forAuto()
                        .licensePlate("PLATE")
                        .build()
        );

        assertEquals("vin is required for AUTO", ex.getMessage());
    }

    @Test
    void shouldFailWhenWrongBranchChosen() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                IncidentBuilder.auto()
                        .occurredAt(Instant.now())
                        .location("Warsaw")
                        .description("x")
                        .forTheft()   // wrong branch
        );

        assertTrue(ex.getMessage().contains("This builder is for AUTO"));
    }
}
