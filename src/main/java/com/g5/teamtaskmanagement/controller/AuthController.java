package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.LoginRequest;
import com.g5.teamtaskmanagement.dto.request.RefreshRequest;
import com.g5.teamtaskmanagement.dto.request.RegisterRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.AccessTokenResponse;
import com.g5.teamtaskmanagement.dto.response.AuthResponse;
import com.g5.teamtaskmanagement.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth Controller", description = "APIs for authentication")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a user account with email, password, and full name")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Register successful", authService.register(req)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate user credentials and return access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse resp = authService.login(req);
        return ResponseEntity.ok(ApiResponse.success("Login successful", resp));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Validate refresh token and issue a new access token")
    public ResponseEntity<ApiResponse<?>> refresh(@Valid @RequestBody RefreshRequest req) {
        String access = authService.refreshAccessToken(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", new AccessTokenResponse(access)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke or remove the provided refresh token")
    public ResponseEntity<ApiResponse<?>> logout(@Valid @RequestBody RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logged out", null));
    }
}
