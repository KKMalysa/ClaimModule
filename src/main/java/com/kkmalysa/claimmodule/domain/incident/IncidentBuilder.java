package com.kkmalysa.claimmodule.domain.incident;

import java.time.Instant;
import java.util.Objects;

/**
 * This is our First Design Pattern - Builder in this project.
 * Step Builder = a Builder variant that enforces the build order and required data at compile time.
 * The build process is split into steps (interfaces), and available methods depend on the current step
 * and chosen IncidentType. This prevents invalid object states and "setter chaos" by construction.
 *
 * Pay attention on all the interfaces, and a class BuilderImpl that implements them all.
 * That's a popular trick in Step Builder - you return "this" but as specific interface.
 * important method - ensureType - prevents mistakes in incident Type.
 */


public final class IncidentBuilder {

    private IncidentBuilder() {}

    // --- ENTRYPOINTS
    public static CommonStep auto() { return new BuilderImpl(IncidentType.AUTO); }
    public static CommonStep theft() { return new BuilderImpl(IncidentType.THEFT); }
    public static CommonStep property() { return new BuilderImpl(IncidentType.PROPERTY); }
    public static CommonStep injury() { return new BuilderImpl(IncidentType.INJURY); }

    // --- STEPS: common
    public interface CommonStep {
        CommonStep occurredAt(Instant occurredAt);
        CommonStep location(String location);

        CommonStep description(String description);

        // go to specific step depends on IncidentType
        AutoStep forAuto();
        TheftStep forTheft();
        PropertyStep forProperty();
        InjuryStep forInjury();
    }

    // --- STEPS: specific
    public interface AutoStep {
        AutoStep vin(String vin);
        AutoStep licensePlate(String plate);
        Incident build();
    }

    public interface TheftStep {
        TheftStep stolenItem(String item);
        TheftStep policeReportNumber(String reportNumber);
        Incident build();
    }

    public interface PropertyStep {
        PropertyStep propertyAddress(String address);
        Incident build();
    }

    public interface InjuryStep {
        InjuryStep injuredPerson(String person);
        InjuryStep medicalReportNumber(String reportNumber);
        Incident build();
    }

    // --- IMPLEMENTATION
    private static final class BuilderImpl implements CommonStep, AutoStep, TheftStep, PropertyStep, InjuryStep {
        private final IncidentType type;

        private Instant occurredAt;
        private String location;
        private String description;

        private String vin;
        private String licensePlate;

        private String stolenItem;
        private String policeReportNumber;

        private String propertyAddress;

        private String injuredPerson;
        private String medicalReportNumber;

        private BuilderImpl(IncidentType type) {
            this.type = Objects.requireNonNull(type);
        }

        @Override
        public CommonStep occurredAt(Instant occurredAt) {
            this.occurredAt = Objects.requireNonNull(occurredAt);
            return this;
        }

        @Override
        public CommonStep location(String location) {
            this.location = requireText(location, "location");
            return this;
        }

        @Override
        public CommonStep description(String description) {
            this.description = requireText(description, "description");
            return this;
        }

        @Override
        public AutoStep forAuto() {
            ensureType(IncidentType.AUTO);
            return this;
        }

        @Override
        public TheftStep forTheft() { ensureType(IncidentType.THEFT); return this; }

        @Override
        public PropertyStep forProperty() { ensureType(IncidentType.PROPERTY); return this; }

        @Override
        public InjuryStep forInjury() { ensureType(IncidentType.INJURY); return this; }

        @Override
        public AutoStep vin(String vin) {
            ensureType(IncidentType.AUTO);
            this.vin = requireText(vin, "vin");
            return this;
        }

        @Override
        public AutoStep licensePlate(String plate) {
            ensureType(IncidentType.AUTO);
            this.licensePlate = requireText(plate, "licensePlate");
            return this;
        }

        @Override
        public TheftStep stolenItem(String item) {
            ensureType(IncidentType.THEFT);
            this.stolenItem = requireText(item, "stolenItem");
            return this;
        }

        @Override
        public TheftStep policeReportNumber(String reportNumber) {
            ensureType(IncidentType.THEFT);
            this.policeReportNumber = requireText(reportNumber, "policeReportNumber");
            return this;
        }

        @Override
        public PropertyStep propertyAddress(String address) {
            ensureType(IncidentType.PROPERTY);
            this.propertyAddress = requireText(address, "propertyAddress");
            return this;
        }

        @Override
        public InjuryStep injuredPerson(String person) {
            ensureType(IncidentType.INJURY);
            this.injuredPerson = requireText(person, "injuredPerson");
            return this;
        }

        @Override
        public InjuryStep medicalReportNumber(String person) {
            ensureType(IncidentType.INJURY);
            this.medicalReportNumber = requireText(person, "medicalReportNumber");
            return this;
        }


        @Override
        public Incident build() {
            // 1) common requirements
            if (occurredAt == null) throw new IllegalStateException("occurredAt is required");
            if (location == null || location.isBlank()) throw new IllegalStateException("location is required");

            // 2) requirements per type
            switch (type) {
                case AUTO -> {
                    if (isBlank(vin)) throw new IllegalStateException("vin is required for AUTO");
                    if (isBlank(licensePlate)) throw new IllegalStateException("licensePlate is required for AUTO");
                }
                case THEFT -> {
                    if (isBlank(stolenItem)) throw new IllegalStateException("stolenItem is required for THEFT");

                }
                case PROPERTY -> {
                    if (isBlank(propertyAddress)) throw new IllegalStateException("propertyAddress is required for PROPERTY");
                }
                case INJURY -> {
                    if (isBlank(injuredPerson)) throw new IllegalStateException("injuredPerson is required for INJURY");

                }
            }

            return new Incident(
                    type, occurredAt, location, description,
                    vin, licensePlate,
                    stolenItem, policeReportNumber,
                    propertyAddress,
                    injuredPerson, medicalReportNumber
            );
        }

        private void ensureType(IncidentType expected) {
            if (this.type != expected) {
                throw new IllegalStateException("This builder is for " + type + ", not " + expected);
            }
        }

        private static String requireText(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
            return value;
        }

        private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    }
}
