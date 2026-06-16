package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.ProjectMemberRequest;
import com.g5.teamtaskmanagement.dto.request.ProjectRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateProjectMemberRoleRequest;
import com.g5.teamtaskmanagement.dto.response.ProjectMemberResponse;
import com.g5.teamtaskmanagement.dto.response.ProjectResponse;
import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.entity.ProjectMember;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.DuplicateResourceException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.exception.ResourceNotFoundException;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceMemberRepository;
import com.g5.teamtaskmanagement.repository.WorkspaceRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;
import com.g5.teamtaskmanagement.service.PermissionService;
import com.g5.teamtaskmanagement.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;

    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository,
            WorkspaceRepository workspaceRepository, WorkspaceMemberRepository workspaceMemberRepository,
            UserRepository userRepository, CurrentUserService currentUserService, PermissionService permissionService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.workspaceRepository = workspaceRepository;
        this.workspaceMemberRepository = workspaceMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public ProjectResponse createProject(Long workspaceId, ProjectRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Workspace workspace = getActiveWorkspace(workspaceId);
        requireWorkspaceOwner(workspaceId, currentUser.getId());
        validateDateRange(request);

        Project project = new Project();
        project.setWorkspace(workspace);
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setCreatedBy(currentUser);
        Project saved = projectRepository.save(project);

        ProjectMember manager = new ProjectMember();
        manager.setWorkspace(workspace);
        manager.setProject(saved);
        manager.setMember(currentUser);
        manager.setRole(ProjectMemberRole.PROJECT_MANAGER);
        manager.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(manager);

        return toProjectResponse(saved, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByWorkspace(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveWorkspace(workspaceId);
        if (!permissionService.isWorkspaceMember(workspaceId, currentUserId)) {
            throw new ForbiddenException("You are not a member of this workspace");
        }

        List<Project> projects = permissionService.isWorkspaceOwner(workspaceId, currentUserId)
                ? projectRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId)
                : projectRepository.findDistinctByWorkspaceIdAndMembersMemberIdAndDeletedAtIsNull(workspaceId,
                        currentUserId);
        return projects.stream()
                .map(project -> toProjectResponse(project, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Project project = getActiveProject(projectId);
        requireCanViewProject(projectId, currentUserId);
        return toProjectResponse(project, currentUserId);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Project project = getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        validateDateRange(request);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        return toProjectResponse(projectRepository.save(project), currentUserId);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Project project = getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        project.setDeletedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveProject(projectId);
        requireCanViewProject(projectId, currentUserId);
        return projectMemberRepository.findByProjectId(projectId).stream()
                .map(this::toProjectMemberResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectMemberResponse addProjectMember(Long projectId, ProjectMemberRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Project project = getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        User user = getUser(request.getUserId());

        Long workspaceId = project.getWorkspace().getId();
        if (!workspaceMemberRepository.existsByWorkspaceIdAndMemberId(workspaceId, user.getId())) {
            throw new BadRequestException("User must be a workspace member before joining this project");
        }
        if (projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, user.getId(), request.getRole())) {
            throw new DuplicateResourceException("User already has this project role");
        }

        ProjectMember member = new ProjectMember();
        member.setWorkspace(project.getWorkspace());
        member.setProject(project);
        member.setMember(user);
        member.setRole(request.getRole());
        member.setJoinedAt(LocalDateTime.now());
        return toProjectMemberResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateProjectMemberRole(Long projectId, Long userId,
            UpdateProjectMemberRoleRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        ProjectMember member = getSingleProjectMember(projectId, userId);
        if (!member.getRole().equals(request.getRole())
                && projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, userId, request.getRole())) {
            throw new DuplicateResourceException("User already has this project role");
        }
        member.setRole(request.getRole());
        return toProjectMemberResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional
    public ProjectMemberResponse addProjectMemberRole(Long projectId, Long userId, ProjectMemberRole role) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Project project = getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        User user = getUser(userId);

        Long workspaceId = project.getWorkspace().getId();
        if (!workspaceMemberRepository.existsByWorkspaceIdAndMemberId(workspaceId, userId)) {
            throw new BadRequestException("User must be a workspace member before joining this project");
        }
        if (projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, userId, role)) {
            throw new DuplicateResourceException("User already has this project role");
        }

        ProjectMember member = new ProjectMember();
        member.setWorkspace(project.getWorkspace());
        member.setProject(project);
        member.setMember(user);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        return toProjectMemberResponse(projectMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeProjectMemberRole(Long projectId, Long userId, ProjectMemberRole role) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        ProjectMember member = projectMemberRepository.findByProjectIdAndMemberIdAndRole(projectId, userId, role)
                .orElseThrow(() -> new ResourceNotFoundException("Project member role not found"));
        projectMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public void removeProjectMember(Long projectId, Long userId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveProject(projectId);
        requireCanManageProject(projectId, currentUserId);
        List<ProjectMember> memberships = projectMemberRepository.findByProjectIdAndMemberId(projectId, userId);
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("Project member not found");
        }
        projectMemberRepository.deleteAll(memberships);
    }

    private Workspace getActiveWorkspace(Long workspaceId) {
        return workspaceRepository.findByIdAndDeletedAtIsNull(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
    }

    private Project getActiveProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ProjectMember getSingleProjectMember(Long projectId, Long userId) {
        List<ProjectMember> memberships = projectMemberRepository.findByProjectIdAndMemberId(projectId, userId);
        if (memberships.isEmpty()) {
            throw new ResourceNotFoundException("Project member not found");
        }
        if (memberships.size() > 1) {
            throw new BadRequestException("User has multiple project roles; remove and add the desired roles instead");
        }
        return memberships.get(0);
    }

    private void requireWorkspaceOwner(Long workspaceId, Long userId) {
        if (!permissionService.isWorkspaceOwner(workspaceId, userId)) {
            throw new ForbiddenException("Only workspace owner can create projects");
        }
    }

    private void requireCanViewProject(Long projectId, Long userId) {
        if (!permissionService.canViewProject(projectId, userId)) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    private void requireCanManageProject(Long projectId, Long userId) {
        if (!permissionService.canManageProject(projectId, userId)) {
            throw new ForbiddenException("You do not have permission to manage this project");
        }
    }

    private void validateDateRange(ProjectRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("startDate must be before or equal to endDate");
        }
    }

    private ProjectResponse toProjectResponse(Project project, Long currentUserId) {
        List<ProjectMemberRole> roles = resolveMyRoles(project, currentUserId);
        ProjectMemberRole role = roles.contains(ProjectMemberRole.PROJECT_MANAGER)
                ? ProjectMemberRole.PROJECT_MANAGER
                : roles.stream().findFirst().orElse(null);
        return new ProjectResponse(project.getId(), project.getWorkspace().getId(), project.getName(),
                project.getDescription(), role, roles, project.getStartDate(), project.getEndDate(),
                project.getCreatedAt(), project.getUpdatedAt());
    }

    private List<ProjectMemberRole> resolveMyRoles(Project project, Long currentUserId) {
        List<ProjectMemberRole> roles = projectMemberRepository.findByProjectIdAndMemberId(project.getId(),
                currentUserId).stream()
                .map(ProjectMember::getRole)
                .distinct()
                .toList();
        if (!roles.isEmpty()) {
            return roles;
        }
        if (permissionService.isWorkspaceOwner(project.getWorkspace().getId(), currentUserId)) {
            return List.of(ProjectMemberRole.PROJECT_MANAGER);
        }
        return List.of();
    }

    private ProjectMemberResponse toProjectMemberResponse(ProjectMember member) {
        User user = member.getMember();
        return new ProjectMemberResponse(member.getId(), user.getId(), user.getEmail(), user.getFullName(),
                member.getRole(), member.getJoinedAt());
    }
}
