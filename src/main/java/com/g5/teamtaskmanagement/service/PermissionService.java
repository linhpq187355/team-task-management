package com.g5.teamtaskmanagement.service;

public interface PermissionService {
    boolean isWorkspaceOwner(Long workspaceId, Long userId);

    boolean isWorkspaceMember(Long workspaceId, Long userId);

    boolean isProjectMember(Long projectId, Long userId);

    boolean isProjectManager(Long projectId, Long userId);

    boolean isWorkspaceOwnerOfProject(Long projectId, Long userId);

    boolean canManageProject(Long projectId, Long userId);

    boolean canViewProject(Long projectId, Long userId);

    boolean canManageTask(Long taskId, Long userId);

    boolean isTaskAssignee(Long taskId, Long userId);
}
