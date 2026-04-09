package com.skinmarket.marketplace.dto.marketitem;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMarketItemRequest(
        UUID skinId,
        BigDecimal price
) {
}
