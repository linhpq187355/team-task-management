package com.g5.teamtaskmanagement.dto.request;

import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import jakarta.validation.constraints.NotNull;

public class UpdateProjectMemberRoleRequest {

    @NotNull
    private ProjectMemberRole role;

    public ProjectMemberRole getRole() {
        return role;
    }

    public void setRole(ProjectMemberRole role) {
        this.role = role;
    }
}
