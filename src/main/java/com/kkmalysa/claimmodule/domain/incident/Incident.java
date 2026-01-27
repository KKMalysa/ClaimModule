package com.kkmalysa.claimmodule.domain.incident;

import lombok.Getter;

import java.time.Instant;
import java.util.Optional;


/**
 * @TODO sealed details + composition instead of a bunch of fields with a lot of nulls
 */
public class Incident {

    private final IncidentType incidentType;

    //obligatory
    private final Instant occurredAt;
    private final String location;

    //optional
    private final String description;

    // IncidentType = AUTO
    private final String vin;
    private final String licensePlate;

    // IncidentType = THEFT
    private final String stolenItem;
    private final String policeReportNumber; //optional

    // IncidentType = PROPERTY
    private final String propertyAddress;

    // IncidentType = INJURY
    private final String injuredPerson;
    private final String medicalReportNumber; // optional

    Incident(IncidentType incidentType, Instant occurredAt, String location, String description, String vin, String licensePlate,
             String stolenItem, String policeReportNumber, String propertyAddress, String injuredPerson, String medicalReportNumber) {
        this.incidentType = incidentType;
        this.occurredAt = occurredAt;
        this.location = location;
        this.description = description;
        this.vin = vin;
        this.licensePlate = licensePlate;
        this.stolenItem = stolenItem;
        this.policeReportNumber = policeReportNumber;
        this.propertyAddress = propertyAddress;
        this.injuredPerson = injuredPerson;
        this.medicalReportNumber = medicalReportNumber;
    }

    public IncidentType getIncidentType() { return incidentType; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getLocation() { return location; }


    public Optional <String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional <String> getVin() {
        return Optional.ofNullable(vin);
    }

    public Optional <String> getLicensePlate() {
        return Optional.ofNullable(licensePlate);
    }

    public Optional <String> getStolenItem() {
        return Optional.ofNullable(stolenItem);
    }

    public Optional <String> getPoliceReportNumber() {
        return Optional.ofNullable(policeReportNumber);
    }

    public Optional <String> getPropertyAddress() {
        return Optional.ofNullable(propertyAddress);
    }

    public Optional <String> getInjuredPerson() {
        return Optional.ofNullable(injuredPerson);
    }

    public Optional <String> getMedicalReportNumber() {
        return Optional.ofNullable(medicalReportNumber);
    }
}
