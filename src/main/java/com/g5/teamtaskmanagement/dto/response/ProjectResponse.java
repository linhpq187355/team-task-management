package com.g5.teamtaskmanagement.dto.response;

import com.g5.teamtaskmanagement.entity.ProjectMemberRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectResponse {
    private Long id;
    private Long workspaceId;
    private String name;
    private String description;
    private ProjectMemberRole myRole;
    private List<ProjectMemberRole> myRoles;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse(Long id, Long workspaceId, String name, String description, ProjectMemberRole myRole,
            List<ProjectMemberRole> myRoles, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.workspaceId = workspaceId;
        this.name = name;
        this.description = description;
        this.myRole = myRole;
        this.myRoles = myRoles;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProjectMemberRole getMyRole() {
        return myRole;
    }

    public List<ProjectMemberRole> getMyRoles() {
        return myRoles;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
