package com.skinmarket.notification.dto;

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
) {}