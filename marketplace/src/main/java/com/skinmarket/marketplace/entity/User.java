package com.skinmarket.marketplace.entity;

import com.skinmarket.marketplace.enums.UserRole;

import java.math.BigDecimal;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String passwordHash,
        BigDecimal balance,
        UserRole role,
        Long version
) {
}
