package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.ProjectMemberRequest;
import com.g5.teamtaskmanagement.dto.request.ProjectRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProjectMemberRoleRequest;
import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.entity.ProjectMember;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceRepository;
import com.g5.teamtaskmanagement.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final WorkspaceRepository workspaceRepository = mock(WorkspaceRepository.class);
    private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final PermissionService permissionService = mock(PermissionService.class);
    private final ProjectService projectService = new ProjectServiceImpl(projectRepository, projectMemberRepository,
            workspaceRepository, workspaceMemberRepository, userRepository, currentUserService, permissionService);

    @Test
    void createProjectRequiresWorkspaceOwnerBeforeSaving() {
        User currentUser = user(1L);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> projectService.createProject(10L, validProjectRequest()));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void createProjectRejectsInvalidDateRange() {
        User currentUser = user(1L);
        ProjectRequest request = validProjectRequest();
        request.setStartDate(LocalDate.now().plusDays(2));
        request.setEndDate(LocalDate.now().plusDays(1));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> projectService.createProject(10L, request));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void addProjectMemberRequiresWorkspaceMembershipAndRejectsDuplicateRole() {
        ProjectMemberRequest request = projectMemberRequest(ProjectMemberRole.DEVELOPER);
        Project project = project(20L, 10L);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(workspaceMemberRepository.existsByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> projectService.addProjectMember(20L, request));

        when(workspaceMemberRepository.existsByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(true);
        when(projectMemberRepository.existsByProjectIdAndMemberIdAndRole(20L, 2L, ProjectMemberRole.DEVELOPER))
                .thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> projectService.addProjectMember(20L, request));
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void updateProjectMemberRoleRejectsWhenUserHasMultipleProjectRoles() {
        UpdateProjectMemberRoleRequest request = new UpdateProjectMemberRoleRequest();
        request.setRole(ProjectMemberRole.PROJECT_MANAGER);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project(20L, 10L)));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdAndMemberId(20L, 2L)).thenReturn(List.of(
                projectMember(20L, 2L, ProjectMemberRole.DEVELOPER),
                projectMember(20L, 2L, ProjectMemberRole.PROJECT_MANAGER)));

        assertThrows(BadRequestException.class, () -> projectService.updateProjectMemberRole(20L, 2L, request));
    }

    @Test
    void workspaceOwnerWithoutProjectRoleResolvesAsProjectManagerInResponse() {
        Project project = project(20L, 10L);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project));
        when(permissionService.canViewProject(20L, 1L)).thenReturn(true);
        when(projectMemberRepository.findByProjectIdAndMemberId(20L, 1L)).thenReturn(List.of());
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);

        assertEquals(ProjectMemberRole.PROJECT_MANAGER, projectService.getProject(20L).getMyRole());
    }

    private ProjectRequest validProjectRequest() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Project");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusDays(1));
        return request;
    }

    private ProjectMemberRequest projectMemberRequest(ProjectMemberRole role) {
        ProjectMemberRequest request = new ProjectMemberRequest();
        request.setUserId(2L);
        request.setRole(role);
        return request;
    }

    private Project project(Long projectId, Long workspaceId) {
        Project project = new Project();
        project.setId(projectId);
        project.setName("Project");
        project.setWorkspace(workspace(workspaceId));
        return project;
    }

    private ProjectMember projectMember(Long projectId, Long userId, ProjectMemberRole role) {
        ProjectMember member = new ProjectMember();
        member.setProject(project(projectId, 10L));
        member.setWorkspace(workspace(10L));
        member.setMember(user(userId));
        member.setRole(role);
        return member;
    }

    private Workspace workspace(Long id) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        return workspace;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@example.com");
        user.setFullName("User " + id);
        return user;
    }
}
