package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.LoginRequest;
import com.g5.teamtaskmanagement.dto.request.RegisterRequest;
import com.g5.teamtaskmanagement.dto.response.AuthResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.security.JwtService;
import com.g5.teamtaskmanagement.service.AuthService;
import com.g5.teamtaskmanagement.service.RefreshTokenIssueResult;
import com.g5.teamtaskmanagement.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
            RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public UserDto register(RegisterRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(u -> {
            throw new DuplicateResourceException("Email already exists");
        });
        User user = new User();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userRepository.save(user);
        return new UserDto(user.getId(), user.getEmail(), user.getFullName());
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }
        RefreshTokenIssueResult refreshTokenIssue = refreshTokenService.createRefreshToken(user);
        String access = jwtService.generateAccessToken(user, refreshTokenIssue.getRefreshTokenId());
        String refresh = refreshTokenIssue.getRefreshToken();
        UserDto ud = new UserDto(user.getId(), user.getEmail(), user.getFullName());
        return new AuthResponse(access, refresh, ud);
    }

    @Override
    public String refreshAccessToken(String rawRefreshToken) {
        return refreshTokenService.findByRawToken(rawRefreshToken)
                .filter(rt -> rt.getRevokedAt() == null && rt.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(rt -> {
                    User user = rt.getUser();
                    return jwtService.generateAccessToken(user, rt.getId());
                })
                .orElseThrow(() -> new BadRequestException("Invalid or expired refresh token"));
    }

    @Override
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }
}
