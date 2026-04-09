package com.skinmarket.marketplace.dto.marketitem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarketItemPurchasedEvent(
        UUID marketItemId,
        UUID sellerId,
        UUID buyerId,
        UUID skinId,
        BigDecimal price,
        LocalDateTime soldAt
) {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public String toJson() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize MarketItemPurchasedEvent", e);
        }
    }
}
