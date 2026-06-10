package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.AddWorkspaceMemberRequest;
import com.g5.teamtaskmanagement.dto.request.CreateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.WorkspaceDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceMemberDto;
import com.g5.teamtaskmanagement.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@Tag(name = "Workspace Controller", description = "APIs for managing workspaces")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @PostMapping
    @Operation(summary = "Create a new workspace", description = "Current user will be set as workspace owner")
    public ResponseEntity<ApiResponse<WorkspaceDto>> createWorkspace(@Valid @RequestBody CreateWorkspaceRequest request) {
        WorkspaceDto workspace = workspaceService.createWorkspace(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Workspace created successfully", workspace));
    }

    @GetMapping
    @Operation(summary = "Get all workspaces of current user")
    public ResponseEntity<ApiResponse<List<WorkspaceDto>>> getUserWorkspaces() {
        List<WorkspaceDto> workspaces = workspaceService.getUserWorkspaces();
        return ResponseEntity.ok(new ApiResponse<>(true, "Workspaces retrieved successfully", workspaces));
    }

    @GetMapping("/{workspaceId}")
    @Operation(summary = "Get workspace details", description = "Only workspace members can view")
    public ResponseEntity<ApiResponse<WorkspaceDto>> getWorkspaceDetail(@PathVariable Long workspaceId) {
        WorkspaceDto workspace = workspaceService.getWorkspaceDetail(workspaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Workspace retrieved successfully", workspace));
    }

    @PutMapping("/{workspaceId}")
    @Operation(summary = "Update workspace details", description = "Only workspace owner can update")
    public ResponseEntity<ApiResponse<WorkspaceDto>> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request) {
        WorkspaceDto workspace = workspaceService.updateWorkspace(workspaceId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Workspace updated successfully", workspace));
    }

    @DeleteMapping("/{workspaceId}")
    @Operation(summary = "Delete workspace", description = "Soft delete - only workspace owner can delete")
    public ResponseEntity<ApiResponse<Void>> deleteWorkspace(@PathVariable Long workspaceId) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Workspace deleted successfully", null));
    }

    @GetMapping("/{workspaceId}/members")
    @Operation(summary = "Get all members of a workspace")
    public ResponseEntity<ApiResponse<List<WorkspaceMemberDto>>> getWorkspaceMembers(@PathVariable Long workspaceId) {
        List<WorkspaceMemberDto> members = workspaceService.getWorkspaceMembers(workspaceId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Members retrieved successfully", members));
    }

    @PostMapping("/{workspaceId}/members")
    @Operation(summary = "Add a member to workspace", description = "Only workspace owner can add members")
    public ResponseEntity<ApiResponse<WorkspaceMemberDto>> addWorkspaceMember(
            @PathVariable Long workspaceId,
            @Valid @RequestBody AddWorkspaceMemberRequest request) {
        WorkspaceMemberDto member = workspaceService.addWorkspaceMember(workspaceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Member added successfully", member));
    }

    @PutMapping("/{workspaceId}/members/{userId}/role")
    @Operation(summary = "Update member role", description = "Only workspace owner can change member role")
    public ResponseEntity<ApiResponse<WorkspaceMemberDto>> updateWorkspaceMemberRole(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateWorkspaceMemberRoleRequest request) {
        WorkspaceMemberDto member = workspaceService.updateWorkspaceMemberRole(workspaceId, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member role updated successfully", member));
    }

    @DeleteMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Remove a member from workspace", description = "Only workspace owner can remove members")
    public ResponseEntity<ApiResponse<Void>> removeWorkspaceMember(
            @PathVariable Long workspaceId,
            @PathVariable Long userId) {
        workspaceService.removeWorkspaceMember(workspaceId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member removed successfully", null));
    }
}
