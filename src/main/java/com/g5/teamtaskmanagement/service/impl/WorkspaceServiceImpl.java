package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.AddWorkspaceMemberRequest;
import com.g5.teamtaskmanagement.dto.request.CreateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceMemberDto;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.entity.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.exception.ResourceNotFoundException;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;
import com.g5.teamtaskmanagement.service.PermissionService;
import com.g5.teamtaskmanagement.service.WorkspaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            ProjectMemberRepository projectMemberRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            PermissionService permissionService) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
    }

    @Override
    public WorkspaceDto createWorkspace(CreateWorkspaceRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Workspace workspace = new Workspace();
        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace.setCreatedBy(currentUser);

        workspace = workspaceRepository.save(workspace);

        // Add current user as OWNER
        WorkspaceMember owner = new WorkspaceMember();
        owner.setWorkspace(workspace);
        owner.setMember(currentUser);
        owner.setRole(WorkspaceMemberRole.OWNER);
        owner.setJoinedAt(LocalDateTime.now());
        workspaceMemberRepository.save(owner);

        return mapToDto(workspace, currentUser.getId());
    }

    @Override
    public List<WorkspaceDto> getUserWorkspaces() {
        Long userId = currentUserService.getCurrentUserId();

        // Get all workspaces where user is a member and workspace is not deleted
        return workspaceMemberRepository.findByMemberId(userId).stream()
                .filter(member -> member.getWorkspace().getDeletedAt() == null)
                .map(member -> mapToDto(member.getWorkspace(), userId))
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceDto getWorkspaceDetail(Long workspaceId) {
        Long userId = currentUserService.getCurrentUserId();

        // Check if user is a member of the workspace
        if (!permissionService.isWorkspaceMember(workspaceId, userId)) {
            throw new ForbiddenException("You don't have permission to view this workspace");
        }

        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        return mapToDto(workspace, userId);
    }

    @Override
    public WorkspaceDto updateWorkspace(Long workspaceId, UpdateWorkspaceRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        // Only owner can update
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can update");
        }

        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspace.setName(request.getName());
        workspace.setDescription(request.getDescription());
        workspace = workspaceRepository.save(workspace);

        return mapToDto(workspace, userId);
    }

    @Override
    public void deleteWorkspace(Long workspaceId) {
        Long userId = currentUserService.getCurrentUserId();

        // Only owner can delete
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can delete");
        }

        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        workspace.setDeletedAt(LocalDateTime.now());
        workspaceRepository.save(workspace);
    }

    @Override
    public List<WorkspaceMemberDto> getWorkspaceMembers(Long workspaceId) {
        Long userId = currentUserService.getCurrentUserId();

        // Check if user is a member
        if (!permissionService.isWorkspaceMember(workspaceId, userId)) {
            throw new ForbiddenException("You don't have permission to view workspace members");
        }

        // Check if workspace exists
        if (!workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId).isPresent()) {
            throw new ResourceNotFoundException("Workspace not found");
        }

        return workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                .map(this::mapToMemberDto)
                .collect(Collectors.toList());
    }

    @Override
    public WorkspaceMemberDto addWorkspaceMember(Long workspaceId, AddWorkspaceMemberRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        // Only owner can add members
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can add members");
        }

        Workspace workspace = workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));

        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if already member
        if (workspaceMemberRepository.existsByWorkspaceIdAndMemberId(workspaceId, newMember.getId())) {
            throw new DuplicateResourceException("User is already a member of this workspace");
        }

        WorkspaceMember workspaceMember = new WorkspaceMember();
        workspaceMember.setWorkspace(workspace);
        workspaceMember.setMember(newMember);
        workspaceMember.setRole(request.getRole());
        workspaceMember.setJoinedAt(LocalDateTime.now());

        workspaceMember = workspaceMemberRepository.save(workspaceMember);

        return mapToMemberDto(workspaceMember);
    }

    @Override
    public WorkspaceMemberDto updateWorkspaceMemberRole(Long workspaceId, Long memberId, UpdateWorkspaceMemberRoleRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        // Only owner can change role
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can change member role");
        }

        // Check if workspace exists
        if (!workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId).isPresent()) {
            throw new ResourceNotFoundException("Workspace not found");
        }

        WorkspaceMember workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this workspace"));

        // Cannot change the only owner
        if (workspaceMember.getRole() == WorkspaceMemberRole.OWNER
                && request.getRole() != WorkspaceMemberRole.OWNER) {
            long ownerCount = workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                    .filter(m -> m.getRole() == WorkspaceMemberRole.OWNER)
                    .count();
            if (ownerCount == 1) {
                throw new BadRequestException("Cannot remove the only owner from workspace");
            }
        }

        workspaceMember.setRole(request.getRole());
        workspaceMember = workspaceMemberRepository.save(workspaceMember);

        return mapToMemberDto(workspaceMember);
    }

    @Override
    public void removeWorkspaceMember(Long workspaceId, Long memberId) {
        Long userId = currentUserService.getCurrentUserId();

        // Only owner can remove members
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can remove members");
        }

        // Check if workspace exists
        if (!workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId).isPresent()) {
            throw new ResourceNotFoundException("Workspace not found");
        }

        WorkspaceMember workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspaceId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this workspace"));

        // Cannot remove the only owner
        if (workspaceMember.getRole() == WorkspaceMemberRole.OWNER) {
            long ownerCount = workspaceMemberRepository.findByWorkspaceId(workspaceId).stream()
                    .filter(m -> m.getRole() == WorkspaceMemberRole.OWNER)
                    .count();
            if (ownerCount == 1) {
                throw new BadRequestException("Cannot remove the only owner from workspace");
            }
        }

        projectMemberRepository.deleteByWorkspaceIdAndMemberId(workspaceId, memberId);
        workspaceMemberRepository.deleteByWorkspaceIdAndMemberId(workspaceId, memberId);
    }

    private WorkspaceDto mapToDto(Workspace workspace, Long userId) {
        WorkspaceDto dto = new WorkspaceDto();
        dto.setId(workspace.getId());
        dto.setName(workspace.getName());
        dto.setDescription(workspace.getDescription());
        dto.setCreatedAt(workspace.getCreatedAt());

        // Get current user's role
        workspaceMemberRepository.findByWorkspaceIdAndMemberId(workspace.getId(), userId)
                .ifPresent(member -> dto.setMyRole(member.getRole()));

        return dto;
    }

    private WorkspaceMemberDto mapToMemberDto(WorkspaceMember member) {
        WorkspaceMemberDto dto = new WorkspaceMemberDto();
        dto.setId(member.getId());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());

        UserDto userDto = new UserDto();
        userDto.setId(member.getMember().getId());
        userDto.setEmail(member.getMember().getEmail());
        userDto.setFullName(member.getMember().getFullName());
        dto.setUser(userDto);

        return dto;
    }
}
