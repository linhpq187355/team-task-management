package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.CreateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.response.WorkItemCardDto;
import com.g5.teamtaskmanagement.dto.response.WorkItemDto;
import com.g5.teamtaskmanagement.entity.WorkItemPriority;
import com.g5.teamtaskmanagement.entity.WorkItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WorkItemService {
    WorkItemDto createWorkItem(Long projectId, CreateWorkItemRequest request);

    Page<WorkItemCardDto> getProjectWorkItems(Long projectId, WorkItemStatus status, WorkItemPriority priority,
            Long assigneeId, String keyword, Pageable pageable);

    WorkItemDto getWorkItem(Long id);

    WorkItemDto updateWorkItem(Long id, UpdateWorkItemRequest request);
}
