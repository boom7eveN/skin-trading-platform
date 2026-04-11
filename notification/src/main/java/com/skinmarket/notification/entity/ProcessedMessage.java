package com.skinmarket.notification.entity;

import com.skinmarket.notification.enums.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessedMessage(UUID id, UUID aggregateId, EventType eventType, LocalDateTime processedAt) {
}
