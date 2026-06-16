package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.ChangePasswordRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProfileRequest;
import com.g5.teamtaskmanagement.dto.response.UserDto;

public interface UserService {
    UserDto getCurrentUserProfile();

    UserDto updateProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);
}
