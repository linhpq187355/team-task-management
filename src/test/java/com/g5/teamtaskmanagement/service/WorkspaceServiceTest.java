package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.AddWorkspaceMemberRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.mapper.WorkspaceMapper;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceRepository;
import com.g5.teamtaskmanagement.service.impl.WorkspaceServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkspaceServiceTest {

    private final WorkspaceRepository workspaceRepository = mock(WorkspaceRepository.class);
    private final WorkspaceMemberRepository workspaceMemberRepository = mock(WorkspaceMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final PermissionService permissionService = mock(PermissionService.class);
    private final WorkspaceMapper workspaceMapper = new WorkspaceMapper(workspaceMemberRepository);
    private final WorkspaceService workspaceService = new WorkspaceServiceImpl(workspaceRepository,
            workspaceMemberRepository, userRepository, currentUserService, permissionService, workspaceMapper);

    @Test
    void addWorkspaceMemberRequiresWorkspaceOwner() {
        AddWorkspaceMemberRequest request = addMemberRequest();
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> workspaceService.addWorkspaceMember(10L, request));

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    void addWorkspaceMemberRejectsExistingMember() {
        AddWorkspaceMemberRequest request = addMemberRequest();
        User newMember = user(2L);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(userRepository.findByEmail("member@example.com")).thenReturn(Optional.of(newMember));
        when(workspaceMemberRepository.existsByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> workspaceService.addWorkspaceMember(10L, request));

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    void updateWorkspaceMemberRoleRejectsDemotingOnlyOwner() {
        UpdateWorkspaceMemberRoleRequest request = new UpdateWorkspaceMemberRoleRequest();
        request.setRole(WorkspaceMemberRole.MEMBER);
        WorkspaceMember owner = workspaceMember(10L, user(2L), WorkspaceMemberRole.OWNER);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(Optional.of(owner));
        when(workspaceMemberRepository.findByWorkspaceId(10L)).thenReturn(List.of(owner));

        assertThrows(BadRequestException.class, () -> workspaceService.updateWorkspaceMemberRole(10L, 2L, request));

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    void removeWorkspaceMemberRejectsRemovingOnlyOwner() {
        WorkspaceMember owner = workspaceMember(10L, user(2L), WorkspaceMemberRole.OWNER);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(Optional.of(owner));
        when(workspaceMemberRepository.findByWorkspaceId(10L)).thenReturn(List.of(owner));

        assertThrows(BadRequestException.class, () -> workspaceService.removeWorkspaceMember(10L, 2L));

        verify(workspaceMemberRepository, never()).deleteByWorkspaceIdAndMemberId(10L, 2L);
    }

    @Test
    void removeWorkspaceMemberAllowsRemovingOwnerWhenAnotherOwnerExists() {
        WorkspaceMember owner = workspaceMember(10L, user(2L), WorkspaceMemberRole.OWNER);
        WorkspaceMember anotherOwner = workspaceMember(10L, user(3L), WorkspaceMemberRole.OWNER);
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(permissionService.isWorkspaceOwner(10L, 1L)).thenReturn(true);
        when(workspaceRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(workspace(10L)));
        when(workspaceMemberRepository.findByWorkspaceIdAndMemberId(10L, 2L)).thenReturn(Optional.of(owner));
        when(workspaceMemberRepository.findByWorkspaceId(10L)).thenReturn(List.of(owner, anotherOwner));

        workspaceService.removeWorkspaceMember(10L, 2L);

        verify(workspaceMemberRepository).deleteByWorkspaceIdAndMemberId(10L, 2L);
    }

    private AddWorkspaceMemberRequest addMemberRequest() {
        AddWorkspaceMemberRequest request = new AddWorkspaceMemberRequest();
        request.setEmail("member@example.com");
        request.setRole(WorkspaceMemberRole.MEMBER);
        return request;
    }

    private Workspace workspace(Long id) {
        Workspace workspace = new Workspace();
        workspace.setId(id);
        workspace.setName("Workspace");
        return workspace;
    }

    private WorkspaceMember workspaceMember(Long workspaceId, User user, WorkspaceMemberRole role) {
        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace(workspaceId));
        member.setMember(user);
        member.setRole(role);
        return member;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("member@example.com");
        user.setFullName("Member");
        return user;
    }
}
