package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProfileRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Return the authenticated user's basic profile information")
    public ResponseEntity<ApiResponse<UserDto>> me() {
        return ResponseEntity.ok(ApiResponse.success("Profile loaded", userService.getCurrentUserProfile()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Update the authenticated user's full name and avatar URL")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userService.updateProfile(req)));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user password", description = "Validate the old password and set a new password for the authenticated user")
    public ResponseEntity<ApiResponse<?>> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(req);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }
}
