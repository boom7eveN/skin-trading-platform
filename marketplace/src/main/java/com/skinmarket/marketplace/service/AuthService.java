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
            throw new BusinessLogicException(
                    ErrorCode.USER_ALREADY_EXISTS,
                    "Username already exists: " + request.username()
            );
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        User user = UserMapper.toEntity(request, encodedPassword);
        if (!userRepository.createUser(user))
            throw new BusinessLogicException(
                    ErrorCode.UNEXPECTED_ERROR,
                    "Failed to create user while registration"
            );

        String accessToken = jwtService.generateAccessToken(user.username(), user.id(), user.role().name());
        return UserMapper.toAuthResponse(user, accessToken);
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findUserByUsername(request.username())
                .orElseThrow(() -> new BusinessLogicException(
                        ErrorCode.USER_NOT_FOUND,
                        "User not found: " + request.username()
                ));

        String accessToken = jwtService.generateAccessToken(user.username(), user.id(), user.role().name());
        return UserMapper.toAuthResponse(user, accessToken);
    }
}