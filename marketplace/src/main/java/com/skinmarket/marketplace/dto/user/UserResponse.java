package com.skinmarket.marketplace.dto.user;

import java.math.BigDecimal;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        BigDecimal balance,
        String role
) {}