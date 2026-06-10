package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProfileRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;
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
    public ResponseEntity<ApiResponse<UserDto>> me() {
        User user = currentUserService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Profile loaded",
                new UserDto(user.getId(), user.getEmail(), user.getFullName())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest req) {
        User user = currentUserService.getCurrentUser();
        user.setFullName(req.getFullName());
        user.setAvatarUrl(req.getAvatarUrl());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                new UserDto(user.getId(), user.getEmail(), user.getFullName())));
    }

    @PutMapping("/me/password")
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
