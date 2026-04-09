package com.skinmarket.marketplace.entity;

import com.skinmarket.marketplace.enums.MarketItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarketItem(
        UUID id,
        UUID sellerId,
        UUID skinId,
        BigDecimal price,
        MarketItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime soldAt,
        Long version) {
}
