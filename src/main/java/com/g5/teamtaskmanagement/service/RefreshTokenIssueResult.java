package com.g5.teamtaskmanagement.service;

public class RefreshTokenIssueResult {

    private final String refreshToken;
    private final Long refreshTokenId;

    public RefreshTokenIssueResult(String refreshToken, Long refreshTokenId) {
        this.refreshToken = refreshToken;
        this.refreshTokenId = refreshTokenId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Long getRefreshTokenId() {
        return refreshTokenId;
    }
}
