package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.ProjectMemberRequest;
import com.g5.teamtaskmanagement.dto.request.ProjectRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProjectMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectMemberResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectResponse;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.service.ProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Project Controller", description = "APIs for managing projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/workspaces/{workspaceId}/projects")
    @Operation(summary = "Create a project", description = "Create a project inside a workspace. Only workspace owners can create projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@PathVariable Long workspaceId,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project created",
                projectService.createProject(workspaceId, request)));
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    @Operation(summary = "Get workspace projects", description = "Workspace owners see all projects; workspace members see only projects they belong to")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("Projects loaded",
                projectService.getProjectsByWorkspace(workspaceId)));
    }

    @GetMapping("/projects/{id}")
    @Operation(summary = "Get project details", description = "Return project details for workspace owners or project members")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project loaded", projectService.getProject(id)));
    }

    @PutMapping("/projects/{id}")
    @Operation(summary = "Update project", description = "Update project details. Only project managers or workspace owners can update")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request)));
    }

    @DeleteMapping("/projects/{id}")
    @Operation(summary = "Delete project", description = "Soft delete a project. Only project managers or workspace owners can delete")
    public ResponseEntity<ApiResponse<?>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }

    @GetMapping("/projects/{id}/members")
    @Operation(summary = "Get project members", description = "Return all members and roles in a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project members loaded", projectService.getProjectMembers(id)));
    }

    @PostMapping("/projects/{id}/members")
    @Operation(summary = "Add project member", description = "Add a workspace member to a project with the requested role")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(@PathVariable Long id,
            @Valid @RequestBody ProjectMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project member added",
                projectService.addProjectMember(id, request)));
    }

    @PutMapping("/projects/{id}/members/{userId}/role")
    @Operation(summary = "Update project member role", description = "Update a project member role when the user has a single project role")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateProjectMemberRole(@PathVariable Long id,
            @PathVariable Long userId, @Valid @RequestBody UpdateProjectMemberRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project member role updated",
                projectService.updateProjectMemberRole(id, userId, request)));
    }

    @PostMapping("/projects/{id}/members/{userId}/roles/{role}")
    @Operation(summary = "Add project member role", description = "Add one role to an existing workspace member in a project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMemberRole(@PathVariable Long id,
            @PathVariable Long userId, @PathVariable ProjectMemberRole role) {
        return ResponseEntity.ok(ApiResponse.success("Project member role added",
                projectService.addProjectMemberRole(id, userId, role)));
    }

    @DeleteMapping("/projects/{id}/members/{userId}/roles/{role}")
    @Operation(summary = "Remove project member role", description = "Remove one specific role from a project member")
    public ResponseEntity<ApiResponse<?>> removeProjectMemberRole(@PathVariable Long id, @PathVariable Long userId,
            @PathVariable ProjectMemberRole role) {
        projectService.removeProjectMemberRole(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Project member role removed", null));
    }

    @DeleteMapping("/projects/{id}/members/{userId}")
    @Operation(summary = "Remove project member", description = "Remove a user from a project and delete all of their project roles")
    public ResponseEntity<ApiResponse<?>> removeProjectMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeProjectMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project member removed", null));
    }
}
