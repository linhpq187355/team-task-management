package com.g5.teamtaskmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.g5.teamtaskmanagement.entity.WorkItemPriority;
import com.g5.teamtaskmanagement.entity.WorkItemStatus;

import java.time.LocalDate;

public class WorkItemCardDto {
    private Long id;
    private String title;
    private WorkItemStatus status;
    private WorkItemPriority priority;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private UserDto assignee;

    public WorkItemCardDto(Long id, String title, WorkItemStatus status, WorkItemPriority priority, LocalDate dueDate,
            UserDto assignee) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignee = assignee;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
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

    public UserDto getAssignee() {
        return assignee;
    }
}
