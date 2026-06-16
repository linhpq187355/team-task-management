package com.g5.teamtaskmanagement.service.impl;

import com.g5.teamtaskmanagement.dto.request.CreateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.response.UserDto;
import com.g5.teamtaskmanagement.dto.response.WorkItemCardDto;
import com.g5.teamtaskmanagement.dto.response.WorkItemDto;
import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.WorkItem;
import com.g5.teamtaskmanagement.enums.WorkItemPriority;
import com.g5.teamtaskmanagement.enums.WorkItemStatus;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.exception.ResourceNotFoundException;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkItemRepository;
import com.g5.teamtaskmanagement.service.CurrentUserService;
import com.g5.teamtaskmanagement.service.PermissionService;
import com.g5.teamtaskmanagement.service.WorkItemService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkItemServiceImpl implements WorkItemService {

    private final WorkItemRepository workItemRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PermissionService permissionService;

    public WorkItemServiceImpl(WorkItemRepository workItemRepository, ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository, UserRepository userRepository,
            CurrentUserService currentUserService, PermissionService permissionService) {
        this.workItemRepository = workItemRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.permissionService = permissionService;
    }

    @Override
    @Transactional
    public WorkItemDto createWorkItem(Long projectId, CreateWorkItemRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        Project project = getActiveProject(projectId);
        requireCanManageProject(projectId, currentUser.getId());
        validateDueDateForCreate(request.getDueDate());

        WorkItem workItem = new WorkItem();
        workItem.setWorkspace(project.getWorkspace());
        workItem.setProject(project);
        workItem.setTitle(request.getTitle());
        workItem.setDescription(request.getDescription());
        workItem.setStatus(WorkItemStatus.TODO);
        workItem.setPriority(resolvePriority(request.getPriority()));
        workItem.setDueDate(request.getDueDate());
        workItem.setAssignee(resolveDeveloperAssignee(projectId, request.getAssigneeId()));
        workItem.setCreatedBy(currentUser);

        return toWorkItemDto(workItemRepository.save(workItem));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkItemCardDto> getProjectWorkItems(Long projectId, WorkItemStatus status,
            WorkItemPriority priority, Long assigneeId, String keyword, Pageable pageable) {
        Long currentUserId = currentUserService.getCurrentUserId();
        getActiveProject(projectId);
        requireCanViewProject(projectId, currentUserId);

        Specification<WorkItem> specification = buildSpecification(projectId, status, priority, assigneeId, keyword);
        return workItemRepository.findAll(specification, pageable)
                .map(this::toWorkItemCardDto);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkItemDto getWorkItem(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireCanViewProject(workItem.getProject().getId(), currentUserId);
        return toWorkItemDto(workItem);
    }

    @Override
    @Transactional
    public WorkItemDto updateWorkItem(Long id, UpdateWorkItemRequest request) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireCanManageTask(id, currentUserId);
        requireEditable(workItem);
        validateDueDateForUpdate(workItem, request.getDueDate());

        workItem.setTitle(request.getTitle());
        workItem.setDescription(request.getDescription());
        workItem.setPriority(resolvePriority(request.getPriority()));
        workItem.setDueDate(request.getDueDate());
        workItem.setAssignee(resolveDeveloperAssignee(workItem.getProject().getId(), request.getAssigneeId()));

        return toWorkItemDto(workItemRepository.save(workItem));
    }

    @Override
    @Transactional
    public WorkItemDto startWorkItem(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireStatus(workItem, WorkItemStatus.TODO, "Only TODO tasks can be started");
        requireAssigned(workItem);
        requireTaskAssignee(id, currentUserId);

        return transitionStatus(workItem, WorkItemStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public WorkItemDto submitWorkItemForReview(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireStatus(workItem, WorkItemStatus.IN_PROGRESS, "Only IN_PROGRESS tasks can be submitted for review");
        requireTaskAssignee(id, currentUserId);

        return transitionStatus(workItem, WorkItemStatus.REVIEW);
    }

    @Override
    @Transactional
    public WorkItemDto approveWorkItem(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireStatus(workItem, WorkItemStatus.REVIEW, "Only REVIEW tasks can be approved");
        requireCanManageTask(id, currentUserId);

        return transitionStatus(workItem, WorkItemStatus.DONE);
    }

    @Override
    @Transactional
    public WorkItemDto requestChanges(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireStatus(workItem, WorkItemStatus.REVIEW, "Only REVIEW tasks can be sent back for changes");
        requireCanManageTask(id, currentUserId);

        return transitionStatus(workItem, WorkItemStatus.IN_PROGRESS);
    }

    @Override
    @Transactional
    public WorkItemDto cancelWorkItem(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        WorkItem workItem = getActiveWorkItem(id);
        requireNotFinal(workItem);
        if (workItem.getStatus() != WorkItemStatus.TODO && workItem.getStatus() != WorkItemStatus.IN_PROGRESS
                && workItem.getStatus() != WorkItemStatus.REVIEW) {
            throw new BadRequestException("Only TODO, IN_PROGRESS, or REVIEW tasks can be cancelled");
        }
        requireCanManageTask(id, currentUserId);

        return transitionStatus(workItem, WorkItemStatus.CANCELLED);
    }

    private Specification<WorkItem> buildSpecification(Long projectId, WorkItemStatus status,
            WorkItemPriority priority, Long assigneeId, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("project").get("id"), projectId));
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (keyword != null && !keyword.isBlank()) {
                String normalizedKeyword = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        normalizedKeyword);
                Predicate descriptionMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        normalizedKeyword);
                predicates.add(criteriaBuilder.or(titleMatch, descriptionMatch));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Project getActiveProject(Long projectId) {
        return projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private WorkItem getActiveWorkItem(Long workItemId) {
        return workItemRepository.findByIdAndDeletedAtIsNull(workItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void requireCanManageProject(Long projectId, Long userId) {
        if (!permissionService.canManageProject(projectId, userId)) {
            throw new ForbiddenException("You do not have permission to manage tasks in this project");
        }
    }

    private void requireCanViewProject(Long projectId, Long userId) {
        if (!permissionService.canViewProject(projectId, userId)) {
            throw new ForbiddenException("You do not have access to this project");
        }
    }

    private void requireCanManageTask(Long taskId, Long userId) {
        if (!permissionService.canManageTask(taskId, userId)) {
            throw new ForbiddenException("You do not have permission to manage this task");
        }
    }

    private void requireTaskAssignee(Long taskId, Long userId) {
        if (!permissionService.isTaskAssignee(taskId, userId)) {
            throw new ForbiddenException("Only the task assignee can perform this action");
        }
    }

    private void requireEditable(WorkItem workItem) {
        if (workItem.getStatus() == WorkItemStatus.DONE || workItem.getStatus() == WorkItemStatus.CANCELLED) {
            throw new BadRequestException("Tasks in DONE or CANCELLED status cannot be updated");
        }
    }

    private void requireAssigned(WorkItem workItem) {
        if (workItem.getAssignee() == null) {
            throw new BadRequestException("Task must have an assignee before it can be started");
        }
    }

    private void requireStatus(WorkItem workItem, WorkItemStatus expectedStatus, String message) {
        requireNotFinal(workItem);
        if (workItem.getStatus() != expectedStatus) {
            throw new BadRequestException(message);
        }
    }

    private void requireNotFinal(WorkItem workItem) {
        if (workItem.getStatus() == WorkItemStatus.DONE || workItem.getStatus() == WorkItemStatus.CANCELLED) {
            throw new BadRequestException("Tasks in DONE or CANCELLED status cannot change status");
        }
    }

    private WorkItemDto transitionStatus(WorkItem workItem, WorkItemStatus targetStatus) {
        workItem.setStatus(targetStatus);
        return toWorkItemDto(workItemRepository.save(workItem));
    }

    private void validateDueDateForCreate(LocalDate dueDate) {
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("dueDate must be today or later");
        }
    }

    private void validateDueDateForUpdate(WorkItem workItem, LocalDate dueDate) {
        if (dueDate == null) {
            return;
        }
        LocalDate createdDate = workItem.getCreatedAt() != null ? workItem.getCreatedAt().toLocalDate()
                : LocalDate.now();
        if (dueDate.isBefore(createdDate)) {
            throw new BadRequestException("dueDate must be after or equal to the task created date");
        }
    }

    private WorkItemPriority resolvePriority(WorkItemPriority priority) {
        return priority != null ? priority : WorkItemPriority.MEDIUM;
    }

    private User resolveDeveloperAssignee(Long projectId, Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        User assignee = getUser(assigneeId);
        if (!projectMemberRepository.existsByProjectIdAndMemberIdAndRole(projectId, assigneeId,
                ProjectMemberRole.DEVELOPER)) {
            throw new BadRequestException("Assignee must be a developer in this project");
        }
        return assignee;
    }

    private WorkItemCardDto toWorkItemCardDto(WorkItem workItem) {
        return new WorkItemCardDto(workItem.getId(), workItem.getTitle(), workItem.getStatus(),
                workItem.getPriority(), workItem.getDueDate(), toUserDto(workItem.getAssignee()));
    }

    private WorkItemDto toWorkItemDto(WorkItem workItem) {
        return new WorkItemDto(workItem.getId(), workItem.getTitle(), workItem.getDescription(),
                workItem.getStatus(), workItem.getPriority(), workItem.getDueDate(), workItem.getProject().getId(),
                workItem.getWorkspace().getId(), toUserDto(workItem.getAssignee()),
                toUserDto(workItem.getCreatedBy()), workItem.getCreatedAt(), workItem.getUpdatedAt());
    }

    private UserDto toUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDto(user.getId(), user.getEmail(), user.getFullName());
    }
}
