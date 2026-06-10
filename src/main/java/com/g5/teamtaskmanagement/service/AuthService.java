package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.LoginRequest;
import com.g5.teamtaskmanagement.dto.request.RegisterRequest;
import com.g5.teamtaskmanagement.dto.response.AuthResponse;
import com.g5.teamtaskmanagement.dto.response.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest req);

    AuthResponse login(LoginRequest req);

    String refreshAccessToken(String rawRefreshToken);

    void logout(String rawRefreshToken);
}
