package com.skinmarket.marketplace.dto.skin;

import java.math.BigDecimal;

public record UpdateSkinRequest(
        String name,
        String weaponType,
        String rarity,
        BigDecimal basePrice
) {}