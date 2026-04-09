package com.skinmarket.marketplace.controller;

import com.skinmarket.marketplace.dto.user.UserBalanceUpdateRequest;
import com.skinmarket.marketplace.dto.user.UserResponse;
import com.skinmarket.marketplace.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{userId}/balance")
    public ResponseEntity<UserResponse> updateUserBalance(
            @PathVariable UUID userId,
            @RequestBody UserBalanceUpdateRequest request) {

        UserResponse response = userService.addBalance(userId, request.amount());
        return ResponseEntity.ok(response);
    }
}
