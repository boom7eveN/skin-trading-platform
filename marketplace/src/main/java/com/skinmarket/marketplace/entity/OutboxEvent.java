package com.skinmarket.marketplace.entity;

import com.skinmarket.marketplace.enums.OutboxEventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        OutboxEventType eventType,
        String payload,
        LocalDateTime createdAt,
        boolean processed,
        LocalDateTime processedAt,
        int retryCount,
        String error
) {
    public static OutboxEvent create(UUID aggregateId, OutboxEventType eventType, String payload) {
        return new OutboxEvent(
                UUID.randomUUID(),
                aggregateId,
                eventType,
                payload,
                LocalDateTime.now(),
                false,
                null,
                0,
                null
        );
    }

}
