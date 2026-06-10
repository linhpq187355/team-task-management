package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.AddWorkspaceMemberRequest;
import com.g5.teamtaskmanagement.dto.request.CreateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.response.WorkspaceDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceMemberDto;

import java.util.List;

public interface WorkspaceService {
    WorkspaceDto createWorkspace(CreateWorkspaceRequest request);

    List<WorkspaceDto> getUserWorkspaces();

    WorkspaceDto getWorkspaceDetail(Long workspaceId);

    WorkspaceDto updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request);

    void deleteWorkspace(Long workspaceId);

    List<WorkspaceMemberDto> getWorkspaceMembers(Long workspaceId);

    WorkspaceMemberDto addWorkspaceMember(Long workspaceId, AddWorkspaceMemberRequest request);

    WorkspaceMemberDto updateWorkspaceMemberRole(Long workspaceId, Long userId, UpdateWorkspaceMemberRoleRequest request);

    void removeWorkspaceMember(Long workspaceId, Long userId);
}
