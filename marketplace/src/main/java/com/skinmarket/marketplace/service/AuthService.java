package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.dto.auth.AuthRequest;
import com.skinmarket.marketplace.dto.auth.AuthResponse;
import com.skinmarket.marketplace.dto.auth.UserRegisterRequest;
import com.skinmarket.marketplace.entity.User;
import com.skinmarket.marketplace.exception.BusinessLogicException;
import com.skinmarket.marketplace.exception.ErrorCode;
import com.skinmarket.marketplace.mapper.UserMapper;
import com.skinmarket.marketplace.repository.UserRepository;
import com.skinmarket.marketplace.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        if (userRepository.findUserByUsername(request.username()).isPresent()) {
            LOGGER.warn("Registration failed: Username already exists - {}", request.username());
            throw new BusinessLogicException(
                    ErrorCode.USER_ALREADY_EXISTS,
                    "Username already exists: " + request.username()
            );
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = UserMapper.toEntity(request, encodedPassword);
        if (!userRepository.createUser(user)) {
            LOGGER.error("Failed to create user in database: {}", request.username());
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    "Failed to create user while registration"
            );
        }
        LOGGER.info("User registered successfully: {} (ID: {})", user.username(), user.id());
        String accessToken = jwtService.generateAccessToken(user.username(), user.id(), user.role().name());
        return UserMapper.toAuthResponse(user, accessToken);
    }

    public AuthResponse login(AuthRequest request) {
        LOGGER.info("Login attempt for user: {}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found: " + request.username()
                ));
        LOGGER.info("User logged in successfully: {} (ID: {}, Role: {})",
                user.username(), user.id(), user.role().name());
        String accessToken = jwtService.generateAccessToken(user.username(), user.id(), user.role().name());
        return UserMapper.toAuthResponse(user, accessToken);
    }
}