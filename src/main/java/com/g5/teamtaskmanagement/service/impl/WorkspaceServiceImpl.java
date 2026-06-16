package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.AddWorkspaceMemberRequest;
import com.g5.teamtaskmanagement.dto.request.CreateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkspaceRequest;
import com.g5.teamtaskmanagement.dto.response.WorkspaceDto;
import com.g5.teamtaskmanagement.dto.response.WorkspaceMemberDto;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.entity.WorkspaceMember;
import com.g5.teamtaskmanagement.enums.WorkspaceMemberRole;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.exception.ResourceNotFoundException;
import com.g5.teamtaskmanagement.mapper.WorkspaceMapper;
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
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;
    private final WorkspaceMapper workspaceMapper;

    public WorkspaceServiceImpl(WorkspaceRepository workspaceRepository,
            WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            PermissionService permissionService,
            WorkspaceMapper workspaceMapper) {
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
        this.workspaceMapper = workspaceMapper;
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

        return workspaceMapper.toDto(workspace, currentUser.getId());
    }

    @Override
    public List<WorkspaceDto> getUserWorkspaces() {
        Long userId = currentUserService.getCurrentUserId();

        // Get all workspaces where user is a member and workspace is not deleted
        return workspaceMemberRepository.findByMemberId(userId).stream()
                .filter(member -> member.getWorkspace().getDeletedAt() == null)
                .map(member -> workspaceMapper.toDto(member.getWorkspace(), userId))
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

        return workspaceMapper.toDto(workspace, userId);
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

        return workspaceMapper.toDto(workspace, userId);
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
                .map(workspaceMapper::toMemberDto)
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

        return workspaceMapper.toMemberDto(workspaceMember);
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

        return workspaceMapper.toMemberDto(workspaceMember);
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

        workspaceMemberRepository.deleteByWorkspaceIdAndMemberId(workspaceId, memberId);
    }

}
