package com.skinmarket.marketplace.mapper;

import com.skinmarket.marketplace.dto.AuthResponse;
import com.skinmarket.marketplace.dto.UserRegisterRequest;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.enums.UserRole;

import java.math.BigDecimal;
import java.util.UUID;

public class UserMapper {

    public static User toEntity(UserRegisterRequest request, String encodedPassword) {
        return new User(
                UUID.randomUUID(),
                request.username(),
                encodedPassword,
                BigDecimal.ZERO,
                UserRole.USER,
                0L
        );
    }

    public static AuthResponse toAuthResponse(User user, String accessToken) {
        return new AuthResponse(
                accessToken,
                user.id(),
                user.username(),
                user.role().name(),
                user.balance()
        );
    }
}