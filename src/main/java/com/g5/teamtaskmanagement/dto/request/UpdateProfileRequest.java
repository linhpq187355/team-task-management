package com.g5.teamtaskmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {
    @NotBlank
    private String fullName;

    private String avatarUrl;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
