package com.g5.teamtaskmanagement.service;

import com.g5.teamtaskmanagement.dto.request.CreateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.request.UpdateWorkItemRequest;
import com.g5.teamtaskmanagement.dto.response.WorkItemDto;
import com.g5.teamtaskmanagement.entity.Project;
import com.g5.teamtaskmanagement.entity.User;
import com.g5.teamtaskmanagement.entity.WorkItem;
import com.g5.teamtaskmanagement.entity.Workspace;
import com.g5.teamtaskmanagement.enums.ProjectMemberRole;
import com.g5.teamtaskmanagement.enums.WorkItemPriority;
import com.g5.teamtaskmanagement.enums.WorkItemStatus;
import com.g5.teamtaskmanagement.exception.BadRequestException;
import com.g5.teamtaskmanagement.exception.ForbiddenException;
import com.g5.teamtaskmanagement.repository.ProjectMemberRepository;
import com.g5.teamtaskmanagement.repository.ProjectRepository;
import com.g5.teamtaskmanagement.repository.UserRepository;
import com.g5.teamtaskmanagement.repository.WorkItemRepository;
import com.g5.teamtaskmanagement.service.impl.WorkItemServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkItemServiceTest {

    private final WorkItemRepository workItemRepository = mock(WorkItemRepository.class);
    private final ProjectRepository projectRepository = mock(ProjectRepository.class);
    private final ProjectMemberRepository projectMemberRepository = mock(ProjectMemberRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentUserService currentUserService = mock(CurrentUserService.class);
    private final PermissionService permissionService = mock(PermissionService.class);
    private final WorkItemService workItemService = new WorkItemServiceImpl(workItemRepository, projectRepository,
            projectMemberRepository, userRepository, currentUserService, permissionService);

    @Test
    void createWorkItemRequiresTaskManager() {
        when(currentUserService.getCurrentUser()).thenReturn(user(1L));
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project()));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> workItemService.createWorkItem(20L, createRequest(null)));

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void createWorkItemRejectsPastDueDateBeforeSaving() {
        CreateWorkItemRequest request = createRequest(null);
        request.setDueDate(LocalDate.now().minusDays(1));
        when(currentUserService.getCurrentUser()).thenReturn(user(1L));
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project()));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> workItemService.createWorkItem(20L, request));

        verify(workItemRepository, never()).save(any());
    }

    @Test
    void createWorkItemRejectsAssigneeWhoIsNotDeveloper() {
        CreateWorkItemRequest request = createRequest(2L);
        when(currentUserService.getCurrentUser()).thenReturn(user(1L));
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project()));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(projectMemberRepository.existsByProjectIdAndMemberIdAndRole(20L, 2L, ProjectMemberRole.DEVELOPER))
                .thenReturn(false);

        assertThrows(BadRequestException.class, () -> workItemService.createWorkItem(20L, request));
    }

    @Test
    void createWorkItemDefaultsStatusAndPriorityAndAllowsNullAssignee() {
        CreateWorkItemRequest request = createRequest(null);
        request.setPriority(null);
        when(currentUserService.getCurrentUser()).thenReturn(user(1L));
        when(projectRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(project()));
        when(permissionService.canManageProject(20L, 1L)).thenReturn(true);
        when(workItemRepository.save(any(WorkItem.class))).thenAnswer(invocation -> {
            WorkItem saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        WorkItemDto result = workItemService.createWorkItem(20L, request);

        assertEquals(WorkItemStatus.TODO, result.getStatus());
        assertEquals(WorkItemPriority.MEDIUM, result.getPriority());
        verify(workItemRepository).save(org.mockito.ArgumentMatchers.argThat(workItem ->
                workItem.getAssignee() == null && workItem.getCreatedBy().getId().equals(1L)));
    }

    @Test
    void updateWorkItemRejectsFinalStatusAndDueDateBeforeCreatedDate() {
        WorkItem done = workItem(WorkItemStatus.DONE, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(done));
        when(permissionService.canManageTask(30L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> workItemService.updateWorkItem(30L, updateRequest(null)));

        WorkItem editable = workItem(WorkItemStatus.TODO, user(2L));
        editable.setCreatedAt(LocalDateTime.of(2026, 6, 18, 10, 0));
        UpdateWorkItemRequest invalidDueDate = updateRequest(null);
        invalidDueDate.setDueDate(LocalDate.of(2026, 6, 17));
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(editable));

        assertThrows(BadRequestException.class, () -> workItemService.updateWorkItem(30L, invalidDueDate));
    }

    @Test
    void startWorkItemRequiresTodoStatusAssigneeAndCurrentUserToBeAssignee() {
        WorkItem inProgress = workItem(WorkItemStatus.IN_PROGRESS, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(inProgress));

        assertThrows(BadRequestException.class, () -> workItemService.startWorkItem(30L));

        WorkItem unassigned = workItem(WorkItemStatus.TODO, null);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(unassigned));

        assertThrows(BadRequestException.class, () -> workItemService.startWorkItem(30L));

        WorkItem assigned = workItem(WorkItemStatus.TODO, user(2L));
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(assigned));
        when(permissionService.isTaskAssignee(30L, 2L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> workItemService.startWorkItem(30L));
    }

    @Test
    void startWorkItemTransitionsTodoToInProgressForAssignee() {
        WorkItem task = workItem(WorkItemStatus.TODO, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(task));
        when(permissionService.isTaskAssignee(30L, 2L)).thenReturn(true);
        when(workItemRepository.save(task)).thenReturn(task);

        assertEquals(WorkItemStatus.IN_PROGRESS, workItemService.startWorkItem(30L).getStatus());
    }

    @Test
    void submitForReviewRequiresInProgressAndAssignee() {
        WorkItem todo = workItem(WorkItemStatus.TODO, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(2L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(todo));

        assertThrows(BadRequestException.class, () -> workItemService.submitWorkItemForReview(30L));

        WorkItem inProgress = workItem(WorkItemStatus.IN_PROGRESS, user(2L));
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(inProgress));
        when(permissionService.isTaskAssignee(30L, 2L)).thenReturn(true);
        when(workItemRepository.save(inProgress)).thenReturn(inProgress);

        assertEquals(WorkItemStatus.REVIEW, workItemService.submitWorkItemForReview(30L).getStatus());
    }

    @Test
    void approveAndRequestChangesRequireReviewAndTaskManager() {
        WorkItem inProgress = workItem(WorkItemStatus.IN_PROGRESS, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(inProgress));

        assertThrows(BadRequestException.class, () -> workItemService.approveWorkItem(30L));

        WorkItem review = workItem(WorkItemStatus.REVIEW, user(2L));
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(review));
        when(permissionService.canManageTask(30L, 1L)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> workItemService.approveWorkItem(30L));

        when(permissionService.canManageTask(30L, 1L)).thenReturn(true);
        when(workItemRepository.save(review)).thenReturn(review);

        assertEquals(WorkItemStatus.DONE, workItemService.approveWorkItem(30L).getStatus());

        WorkItem reviewAgain = workItem(WorkItemStatus.REVIEW, user(2L));
        when(workItemRepository.findByIdAndDeletedAtIsNull(31L)).thenReturn(Optional.of(reviewAgain));
        when(permissionService.canManageTask(31L, 1L)).thenReturn(true);
        when(workItemRepository.save(reviewAgain)).thenReturn(reviewAgain);

        assertEquals(WorkItemStatus.IN_PROGRESS, workItemService.requestChanges(31L).getStatus());
    }

    @Test
    void cancelWorkItemRejectsFinalStateAndTransitionsAllowedStatuses() {
        WorkItem done = workItem(WorkItemStatus.DONE, user(2L));
        when(currentUserService.getCurrentUserId()).thenReturn(1L);
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(done));

        assertThrows(BadRequestException.class, () -> workItemService.cancelWorkItem(30L));

        WorkItem review = workItem(WorkItemStatus.REVIEW, user(2L));
        when(workItemRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(review));
        when(permissionService.canManageTask(30L, 1L)).thenReturn(true);
        when(workItemRepository.save(review)).thenReturn(review);

        assertEquals(WorkItemStatus.CANCELLED, workItemService.cancelWorkItem(30L).getStatus());
    }

    private CreateWorkItemRequest createRequest(Long assigneeId) {
        CreateWorkItemRequest request = new CreateWorkItemRequest();
        request.setTitle("Task");
        request.setDescription("Description");
        request.setDueDate(LocalDate.now());
        request.setAssigneeId(assigneeId);
        return request;
    }

    private UpdateWorkItemRequest updateRequest(Long assigneeId) {
        UpdateWorkItemRequest request = new UpdateWorkItemRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setPriority(WorkItemPriority.HIGH);
        request.setDueDate(LocalDate.now());
        request.setAssigneeId(assigneeId);
        return request;
    }

    private WorkItem workItem(WorkItemStatus status, User assignee) {
        WorkItem workItem = new WorkItem();
        workItem.setId(30L);
        workItem.setWorkspace(workspace());
        workItem.setProject(project());
        workItem.setTitle("Task");
        workItem.setStatus(status);
        workItem.setPriority(WorkItemPriority.MEDIUM);
        workItem.setAssignee(assignee);
        workItem.setCreatedBy(user(1L));
        workItem.setCreatedAt(LocalDateTime.now());
        return workItem;
    }

    private Project project() {
        Project project = new Project();
        project.setId(20L);
        project.setWorkspace(workspace());
        return project;
    }

    private Workspace workspace() {
        Workspace workspace = new Workspace();
        workspace.setId(10L);
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
