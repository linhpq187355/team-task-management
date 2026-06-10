package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.CreateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.WorkItemCardDto;
import com.g5.teamtaskmanagement.dto.response.WorkItemDto;
import com.g5.teamtaskmanagement.entity.WorkItemPriority;
import com.g5.teamtaskmanagement.entity.WorkItemStatus;
import com.g5.teamtaskmanagement.service.WorkItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Task Controller", description = "APIs for managing project tasks")
@SecurityRequirement(name = "bearerAuth")
public class WorkItemController {

    private final WorkItemService workItemService;

    public WorkItemController(WorkItemService workItemService) {
        this.workItemService = workItemService;
    }

    @PostMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Create task", description = "Create a task in a project. Only project managers or workspace owners can create tasks")
    public ResponseEntity<ApiResponse<WorkItemDto>> createWorkItem(@PathVariable Long projectId,
            @Valid @RequestBody CreateWorkItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task created",
                workItemService.createWorkItem(projectId, request)));
    }

    @GetMapping("/projects/{projectId}/tasks")
    @Operation(summary = "Get project tasks", description = "Return project tasks with status, priority, assignee, keyword, page, size, and sort filters")
    public ResponseEntity<ApiResponse<Page<WorkItemCardDto>>> getProjectWorkItems(@PathVariable Long projectId,
            @RequestParam(required = false) WorkItemStatus status,
            @RequestParam(required = false) WorkItemPriority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Tasks loaded",
                workItemService.getProjectWorkItems(projectId, status, priority, assigneeId, keyword, pageable)));
    }

    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task details", description = "Return task details for workspace owners or project members")
    public ResponseEntity<ApiResponse<WorkItemDto>> getWorkItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Task loaded", workItemService.getWorkItem(id)));
    }

    @PutMapping("/tasks/{id}")
    @Operation(summary = "Update task", description = "Update task details. Final-state tasks cannot be edited")
    public ResponseEntity<ApiResponse<WorkItemDto>> updateWorkItem(@PathVariable Long id,
            @Valid @RequestBody UpdateWorkItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", workItemService.updateWorkItem(id, request)));
    }
}
