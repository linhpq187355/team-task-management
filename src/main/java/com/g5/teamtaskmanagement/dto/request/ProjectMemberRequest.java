package com.g5.teamtaskmanagement.dto.request;

import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

public class ProjectMemberRequest {

    @NotNull
    private Long userId;

    @NotNull
    private ProjectMemberRole role;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ProjectMemberRole getRole() {
        return role;
    }

    public void setRole(ProjectMemberRole role) {
        this.role = role;
    }
}
