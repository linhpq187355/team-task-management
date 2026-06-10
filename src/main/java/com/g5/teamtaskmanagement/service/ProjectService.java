package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.ProjectMemberRequest;
import com.g5.teamtaskmanagement.dto.request.ProjectRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProjectMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.response.ProjectMemberResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectResponse;

import java.util.List;

public interface ProjectService {
    ProjectResponse createProject(Long workspaceId, ProjectRequest request);

    List<ProjectResponse> getProjectsByWorkspace(Long workspaceId);

    ProjectResponse getProject(Long projectId);

    ProjectResponse updateProject(Long projectId, ProjectRequest request);

    void deleteProject(Long projectId);

    List<ProjectMemberResponse> getProjectMembers(Long projectId);

    ProjectMemberResponse addProjectMember(Long projectId, ProjectMemberRequest request);

    ProjectMemberResponse updateProjectMemberRole(Long projectId, Long userId, UpdateProjectMemberRoleRequest request);

    void removeProjectMember(Long projectId, Long userId);
}
