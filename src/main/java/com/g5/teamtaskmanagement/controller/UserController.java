package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProfileRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Return the authenticated user's basic profile information")
    public ResponseEntity<ApiResponse<UserDto>> me() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Profile loaded",
                new UserDto(user.getId(), user.getEmail(), user.getFullName())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update the authenticated user's full name and avatar URL")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User user = currentUserService.getCurrentUser();
        user.setFullName(req.getFullName());
        user.setAvatarUrl(req.getAvatarUrl());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                new UserDto(user.getId(), user.getEmail(), user.getFullName())));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user password", description = "Validate the old password and set a new password for the authenticated user")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password does not match");
        }
        if (passwordEncoder.matches(req.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from old password");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }
}
