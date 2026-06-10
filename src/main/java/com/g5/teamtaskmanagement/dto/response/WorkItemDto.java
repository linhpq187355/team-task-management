package com.g5.teamtaskmanagement.dto.response;

import com.g5.teamtaskmanagement.entity.WorkItemPriority;
import com.g5.teamtaskmanagement.entity.WorkItemStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkItemDto {
    private Long id;
    private String title;
    private String description;
    private WorkItemStatus status;
    private WorkItemPriority priority;
    private LocalDate dueDate;
    private Long projectId;
    private Long workspaceId;
    private UserDto assignee;
    private UserDto createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public WorkItemDto(Long id, String title, String description, WorkItemStatus status, WorkItemPriority priority,
            LocalDate dueDate, Long projectId, Long workspaceId, UserDto assignee, UserDto createdBy,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.projectId = projectId;
        this.workspaceId = workspaceId;
        this.assignee = assignee;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public WorkItemStatus getStatus() {
        return status;
    }

    public WorkItemPriority getPriority() {
        return priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public UserDto getAssignee() {
        return assignee;
    }

    public UserDto getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
