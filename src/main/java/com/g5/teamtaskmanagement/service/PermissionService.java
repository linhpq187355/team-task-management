package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.entity.ProjectMemberRole;
import com.g5.teamtaskmanagement.entity.WorkItem;
import com.g5.teamtaskmanagement.entity.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.WorkItemRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final WorkItemRepository workItemRepository;

    public PermissionService(WorkspaceMemberRepository workspaceMemberRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectRepository projectRepository,
            WorkItemRepository workItemRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.workItemRepository = workItemRepository;
    }

    public boolean isWorkspaceOwner(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceMemberRepository.existsByWorkspaceIdAndMemberIdAndRole(workspaceId, userId,
                WorkspaceMemberRole.OWNER);
    }

    public boolean isWorkspaceMember(Long workspaceId, Long userId) {
        if (workspaceId == null || userId == null) {
            return false;
        }
        return workspaceMemberRepository.existsByWorkspaceIdAndMemberId(workspaceId, userId);
    }

    public boolean isProjectMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectMemberRepository.existsByProjectIdAndMemberId(projectId, userId);
    }

    public boolean isProjectManager(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        return projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, userId,
                ProjectMemberRole.PROJECT_MANAGER);
    }

    public boolean isWorkspaceOwnerOfProject(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        Optional<Project> project = projectRepository.findByIdAndDeletedAtIsNull(projectId);
        return project
                .map(value -> isWorkspaceOwner(value.getWorkspace().getId(), userId))
                .orElse(false);
    }

    public boolean canManageProject(Long projectId, Long userId) {
        return isProjectManager(projectId, userId) || isWorkspaceOwnerOfProject(projectId, userId);
    }

    public boolean canViewProject(Long projectId, Long userId) {
        return isProjectMember(projectId, userId) || isWorkspaceOwnerOfProject(projectId, userId);
    }

    public boolean canManageTask(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return false;
        }
        return workItemRepository.findByIdAndDeletedAtIsNull(taskId)
                .map(workItem -> canManageProject(workItem.getProject().getId(), userId))
                .orElse(false);
    }

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
