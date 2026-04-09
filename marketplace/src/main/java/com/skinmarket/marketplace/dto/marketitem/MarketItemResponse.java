package com.skinmarket.marketplace.dto.marketitem;

import com.skinmarket.marketplace.enums.MarketItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MarketItemResponse(
        UUID id,
        UUID sellerId,
        UUID skinId,
        BigDecimal price,
        MarketItemStatus status,
        LocalDateTime createdAt,
        LocalDateTime soldAt) {
}



