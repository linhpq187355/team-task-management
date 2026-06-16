package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProfileRequest;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;
import com.g5.teamtaskmanagement.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUserProfile() {
        return toUserDto(currentUserService.getCurrentUser());
    }

    @Override
    public UserDto updateProfile(UpdateProfileRequest request) {
        User user = currentUserService.getCurrentUser();
        user.setFullName(request.getFullName());
        user.setAvatarUrl(request.getAvatarUrl());
        return toUserDto(userRepository.save(user));
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserService.getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Old password does not match");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("New password must be different from old password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserDto toUserDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFullName());
    }
}
