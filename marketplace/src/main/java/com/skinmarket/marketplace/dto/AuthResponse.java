package com.skinmarket.marketplace.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        UUID id,
        String username,
        String role,
        BigDecimal balance) {
}
