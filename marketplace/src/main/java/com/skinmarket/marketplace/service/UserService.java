package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.user.UserResponse;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.UserMapper;
import com.skinmarket.marketplace.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse addBalance(UUID userId, BigDecimal amount) {
        LOGGER.info("Attempting to add balance - userId: {}, amount: {}", userId, amount);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            LOGGER.warn("Invalid amount for balance addition - userId: {}, amount: {}", userId, amount);
            throw new BusinessLogicException(
                    ErrorCode.VALIDATION_ERROR,
                    "Amount must be positive."
            );
        }

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found when adding balance - userId: {}", userId);
                    return new BusinessLogicException(
                            ErrorCode.USER_NOT_FOUND,
                            String.format("User with id %s not found", userId)
                    );
                });

        BigDecimal newBalance = user.balance().add(amount);

        Boolean updated = userRepository.updateUserBalanceWithOptimisticLock(
                user.id(),
                newBalance,
                user.version()
        );

        if (!updated) {
            LOGGER.warn("Optimistic lock failure when updating balance - userId: {}, version: {}, amount: {}",
                    userId, user.version(), amount);
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    String.format("Failed to update user balance. Try again. User id: %s",
                            userId)
            );
        }

        User updatedUser = userRepository.findUserById(userId)
                .orElseThrow(() -> {
                    LOGGER.error("User not found after successful balance update - userId: {}", userId);
                    return new BusinessLogicException(
                            ErrorCode.USER_NOT_FOUND,
                            String.format("Updated user with id %s not found", userId)
                    );
                });

        LOGGER.info("Balance added successfully - userId: {}, old balance: {}, amount added: {}, " +
                        "new balance: {}, new version: {}",
                userId,updatedUser.balance(), amount, updatedUser.balance(), updatedUser.version());


        return UserMapper.toResponse(updatedUser);
    }
}