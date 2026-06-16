package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.entity.WorkItem;
import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.WorkItemRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;

    public PermissionServiceImpl(WorkspaceMemberRepository workspaceMemberRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            WorkItemRepository workItemRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.workItemRepository = workItemRepository;
    }

    @Override
    public boolean isWorkspaceOwner(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceMemberRepository.existsByWorkspaceIdAndMemberIdAndRole(workspaceId, userId,
                WorkspaceMemberRole.OWNER);
    }

    @Override
    public boolean isWorkspaceMember(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceMemberRepository.existsByWorkspaceIdAndMemberId(workspaceId, userId);
    }

    @Override
    public boolean isProjectMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectMemberRepository.existsByProjectIdAndMemberId(projectId, userId);
    }

    @Override
    public boolean isProjectManager(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, userId,
                ProjectMemberRole.PROJECT_MANAGER);
    }

    @Override
    public boolean isWorkspaceOwnerOfProject(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        Optional<Project> project = projectRepository.findByIdAndDeletedAtIsNull(projectId);
        return project
                .map(value -> isWorkspaceOwner(value.getWorkspace().getId(), userId))
                .orElse(false);
    }

    @Override
    public boolean canManageProject(Long projectId, Long userId) {
        return isProjectManager(projectId, userId) || isWorkspaceOwnerOfProject(projectId, userId);
    }

    @Override
    public boolean canViewProject(Long projectId, Long userId) {
        return isProjectMember(projectId, userId) || isWorkspaceOwnerOfProject(projectId, userId);
    }

    @Override
    public boolean canManageTask(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return false;
        }
        return workItemRepository.findByIdAndDeletedAtIsNull(taskId)
                .map(workItem -> canManageProject(workItem.getProject().getId(), userId))
                .orElse(false);
    }

    @Override
    public boolean isTaskAssignee(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return false;
        }
        return workItemRepository.findByIdAndDeletedAtIsNull(taskId)
                .map(WorkItem::getAssignee)
                .map(assignee -> userId.equals(assignee.getId()))
                .orElse(false);
    }
}
