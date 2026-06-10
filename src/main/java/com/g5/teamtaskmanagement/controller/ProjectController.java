package com.g5.teamtaskmanagement.controller;

import com.g5.teamtaskmanagement.dto.request.ProjectMemberRequest;
import com.g5.teamtaskmanagement.dto.request.ProjectRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProjectMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.response.ApiResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectMemberResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectResponse;
import com.g5.teamtaskmanagement.service.ProjectService;
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
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/workspaces/{workspaceId}/projects")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@PathVariable Long workspaceId,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project created",
                projectService.createProject(workspaceId, request)));
    }

    @GetMapping("/workspaces/{workspaceId}/projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByWorkspace(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(ApiResponse.success("Projects loaded",
                projectService.getProjectsByWorkspace(workspaceId)));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project loaded", projectService.getProject(id)));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(@PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project updated", projectService.updateProject(id, request)));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", null));
    }

    @GetMapping("/projects/{id}/members")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project members loaded", projectService.getProjectMembers(id)));
    }

    @PostMapping("/projects/{id}/members")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addProjectMember(@PathVariable Long id,
            @Valid @RequestBody ProjectMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project member added",
                projectService.addProjectMember(id, request)));
    }

    @PutMapping("/projects/{id}/members/{userId}/role")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateProjectMemberRole(@PathVariable Long id,
            @PathVariable Long userId, @Valid @RequestBody UpdateProjectMemberRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project member role updated",
                projectService.updateProjectMemberRole(id, userId, request)));
    }

    @DeleteMapping("/projects/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<?>> removeProjectMember(@PathVariable Long id, @PathVariable Long userId) {
        projectService.removeProjectMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Project member removed", null));
    }
}
