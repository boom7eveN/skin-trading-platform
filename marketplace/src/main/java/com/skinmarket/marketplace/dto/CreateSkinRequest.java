package com.skinmarket.marketplace.dto;

import java.math.BigDecimal;

public record CreateSkinRequest(
        String name,
        String weaponType,
        String rarity,
        BigDecimal basePrice
) {}
