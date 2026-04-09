package com.skinmarket.marketplace.dto.user;

import java.math.BigDecimal;

public record UserBalanceUpdateRequest(
        BigDecimal amount
) {}
