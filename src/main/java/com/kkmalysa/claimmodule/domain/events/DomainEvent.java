package com.kkmalysa.claimmodule.domain.events;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
    String eventType();
}
