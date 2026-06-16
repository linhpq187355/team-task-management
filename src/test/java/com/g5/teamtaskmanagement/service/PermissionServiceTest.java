package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.WorkItem;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.WorkItemRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionServiceTest {

    private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final WorkItemRepository workItemRepository = mock(WorkItemRepository.class);
    private final PermissionService permissionService = new PermissionServiceImpl(workspaceMemberRepository,
            projectMemberRepository, projectRepository, workItemRepository);

    @Test
    void workspaceOwnerCanManageAndViewProjectWithoutProjectMembership() {
        Project project = projectWithWorkspace(10L, 3L);
        when(projectRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(project));
        when(workspaceMemberRepository.existsByWorkspaceIdAndMemberIdAndRole(3L, 5L, WorkspaceMemberRole.OWNER))
                .thenReturn(true);

        assertTrue(permissionService.isWorkspaceOwnerOfProject(10L, 5L));
        assertTrue(permissionService.canManageProject(10L, 5L));
        assertTrue(permissionService.canViewProject(10L, 5L));
    }

    @Test
    void projectManagerCanManageProject() {
        when(projectMemberRepository.existsByProjectIdAndMemberIdAndRole(10L, 5L,
                ProjectMemberRole.PROJECT_MANAGER)).thenReturn(true);

        assertTrue(permissionService.isProjectManager(10L, 5L));
        assertTrue(permissionService.canManageProject(10L, 5L));
    }

    @Test
    void workspaceOwnerCanManageTaskThroughProject() {
        Project project = projectWithWorkspace(10L, 3L);
        WorkItem workItem = new WorkItem();
        workItem.setId(20L);
        workItem.setProject(project);

        when(workItemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(workItem));
        when(projectRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(project));
        when(workspaceMemberRepository.existsByWorkspaceIdAndMemberIdAndRole(3L, 5L, WorkspaceMemberRole.OWNER))
                .thenReturn(true);

        assertTrue(permissionService.canManageTask(20L, 5L));
    }

    @Test
    void isTaskAssigneeChecksAssigneeId() {
        User assignee = new User();
        assignee.setId(5L);

        WorkItem workItem = new WorkItem();
        workItem.setId(20L);
        workItem.setAssignee(assignee);

        when(workItemRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(workItem));

        assertTrue(permissionService.isTaskAssignee(20L, 5L));
        assertFalse(permissionService.isTaskAssignee(20L, 6L));
    }

    private Project projectWithWorkspace(Long projectId, Long workspaceId) {
        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);

        Project project = new Project();
        project.setId(projectId);
        project.setWorkspace(workspace);
        return project;
    }
}
