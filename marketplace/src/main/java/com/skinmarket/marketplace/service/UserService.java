package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.user.UserResponse;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.UserMapper;
import com.skinmarket.marketplace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse addBalance(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException(
                    ErrorCode.VALIDATION_ERROR,
                    "Amount must be positive."
            );
        }

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("User with id %s not found", userId)
                ));

        BigDecimal newBalance = user.balance().add(amount);

        Boolean updated = userRepository.updateUserBalanceWithOptimisticLock(
                user.id(),
                newBalance,
                user.version()
        );

        if (!updated) {
            throw new BusinessLogicException(
                    ErrorCode.OPTIMISTIC_LOCK_FAILURE,
                    String.format("Failed to update user balance. Try again. User id: %s",
                            userId)
            );
        }

        User updatedUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("Updated user with id %s not found", userId)
                ));

        return UserMapper.toResponse(updatedUser);
    }
}