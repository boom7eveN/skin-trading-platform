package com.skinmarket.marketplace.dto.auth;

public record UserRegisterRequest(
        String username,
        String password
) {
}
