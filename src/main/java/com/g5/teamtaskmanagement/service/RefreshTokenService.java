package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.RefreshToken;
import com.g5.teamtaskmanagement.entity.User;

import java.util.Optional;

public interface RefreshTokenService {
    RefreshTokenIssueResult createRefreshToken(User user);

    Optional<RefreshToken> findByRawToken(String raw);

    void revoke(String raw);
}
