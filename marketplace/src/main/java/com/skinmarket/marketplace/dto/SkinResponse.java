package com.skinmarket.marketplace.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SkinResponse(
        UUID id,
        String name,
        String weaponType,
        String rarity,
        BigDecimal basePrice
) {}