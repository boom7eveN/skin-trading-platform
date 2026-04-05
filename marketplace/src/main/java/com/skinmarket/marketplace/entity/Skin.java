package com.skinmarket.marketplace.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record Skin(
        UUID id,
        String name,
        String weaponType,
        String rarity,
        BigDecimal basePrice,
        Long version) {
}