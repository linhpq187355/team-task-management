package com.g5.teamtaskmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;

import java.time.LocalDateTime;

public class ProjectMemberResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private ProjectMemberRole role;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;

    public ProjectMemberResponse(Long id, Long userId, String email, String fullName, ProjectMemberRole role,
            LocalDateTime joinedAt) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public ProjectMemberRole getRole() {
        return role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
