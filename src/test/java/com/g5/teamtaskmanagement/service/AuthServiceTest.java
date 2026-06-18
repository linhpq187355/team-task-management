package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.LoginRequest;
import com.g5.teamtaskmanagement.dto.request.RegisterRequest;
import com.g5.teamtaskmanagement.dto.response.AuthResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.entity.RefreshToken;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.security.JwtService;
import com.g5.teamtaskmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final AuthService authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService,
            refreshTokenService);

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user(1L)));

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerHashesPasswordAndReturnsUserDto() {
        RegisterRequest request = registerRequest();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123!")).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(9L);
            return saved;
        });

        UserDto result = authService.register(request);

        assertEquals(9L, result.getId());
        assertEquals("user@example.com", result.getEmail());
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                "hash".equals(user.getPasswordHash()) && "Test User".equals(user.getFullName())));
    }

    @Test
    void loginThrowsWhenEmailMissingOrPasswordDoesNotMatch() {
        LoginRequest request = loginRequest();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(request));

        User user = user(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "hash")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.login(request));
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void loginIssuesRefreshTokenAndAccessToken() {
        LoginRequest request = loginRequest();
        User user = user(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Secret123!", "hash")).thenReturn(true);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(new RefreshTokenIssueResult("refresh", 33L));
        when(jwtService.generateAccessToken(user, 33L)).thenReturn("access");

        AuthResponse response = authService.login(request);

        assertEquals("access", response.getAccessToken());
        assertEquals("refresh", response.getRefreshToken());
        assertEquals(7L, response.getUser().getId());
    }

    @Test
    void refreshAccessTokenRejectsMissingRevokedOrExpiredToken() {
        when(refreshTokenService.findByRawToken("raw")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> authService.refreshAccessToken("raw"));

        RefreshToken revoked = refreshToken(user(1L), LocalDateTime.now().plusDays(1));
        revoked.setRevokedAt(LocalDateTime.now());
        when(refreshTokenService.findByRawToken("raw")).thenReturn(Optional.of(revoked));
        assertThrows(BadRequestException.class, () -> authService.refreshAccessToken("raw"));

        RefreshToken expired = refreshToken(user(1L), LocalDateTime.now().minusSeconds(1));
        when(refreshTokenService.findByRawToken("raw")).thenReturn(Optional.of(expired));
        assertThrows(BadRequestException.class, () -> authService.refreshAccessToken("raw"));
    }

    @Test
    void refreshAccessTokenGeneratesTokenForValidRefreshToken() {
        User user = user(4L);
        RefreshToken refreshToken = refreshToken(user, LocalDateTime.now().plusDays(1));
        refreshToken.setId(55L);
        when(refreshTokenService.findByRawToken("raw")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken(user, 55L)).thenReturn("new-access");

        assertEquals("new-access", authService.refreshAccessToken("raw"));
    }

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Secret123!");
        request.setFullName("Test User");
        return request;
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Secret123!");
        return request;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        user.setFullName("Test User");
        user.setPasswordHash("hash");
        return user;
    }

    private RefreshToken refreshToken(User user, LocalDateTime expiresAt) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(expiresAt);
        return refreshToken;
    }
}
