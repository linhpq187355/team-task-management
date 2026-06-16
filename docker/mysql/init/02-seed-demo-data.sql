CREATE DATABASE IF NOT EXISTS team_task_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE team_task_management;

-- Demo seed data for Swagger testing.
-- Run after docker/mysql/init/01-reset-database.sql or after Hibernate has created the schema.
-- All demo users use the same password: password
-- BCrypt hash below is for plain text "password".

SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM notifications WHERE id BETWEEN 9001 AND 9999;
DELETE FROM workspace_invitations WHERE id BETWEEN 8001 AND 8999;
DELETE FROM work_item_activity_logs WHERE id BETWEEN 7001 AND 7999;
DELETE FROM work_item_attachments WHERE id BETWEEN 6001 AND 6999;
DELETE FROM work_item_comments WHERE id BETWEEN 5001 AND 5999;
DELETE FROM work_items WHERE id BETWEEN 4001 AND 4999;
DELETE FROM project_members WHERE id BETWEEN 30001 AND 39999;
DELETE FROM projects WHERE id BETWEEN 3001 AND 3999;
DELETE FROM workspace_members WHERE id BETWEEN 20001 AND 29999;
DELETE FROM workspaces WHERE id BETWEEN 2001 AND 2999;
DELETE FROM refresh_tokens WHERE user_id BETWEEN 1001 AND 1099;
DELETE FROM users WHERE id BETWEEN 1001 AND 1099;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. USERS
-- =========================================================

INSERT INTO users (id, email, password_hash, full_name, avatar_url, status, created_at, updated_at)
VALUES
    (1001, 'owner.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Owner Demo', NULL, 'ACTIVE', '2026-06-01 08:00:00', '2026-06-01 08:00:00'),
    (1002, 'pm.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Project Manager Demo', NULL, 'ACTIVE', '2026-06-01 08:05:00', '2026-06-01 08:05:00'),
    (1003, 'dev1.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Developer One Demo', NULL, 'ACTIVE', '2026-06-01 08:10:00', '2026-06-01 08:10:00'),
    (1004, 'dev2.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Developer Two Demo', NULL, 'ACTIVE', '2026-06-01 08:15:00', '2026-06-01 08:15:00'),
    (1005, 'member.no.project@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Workspace Only Member', NULL, 'ACTIVE', '2026-06-01 08:20:00', '2026-06-01 08:20:00'),
    (1006, 'outsider.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Outsider Demo', NULL, 'ACTIVE', '2026-06-01 08:25:00', '2026-06-01 08:25:00'),
    (1007, 'disabled.demo@g5.local', '$2a$10$1EwQ9WQi2R7veXWG.aPRo.fpDMnITrqs38gSUcLguYOoEVg1Ecwf6', 'Disabled Demo', NULL, 'DISABLED', '2026-06-01 08:30:00', '2026-06-01 08:30:00');

-- =========================================================
-- 2. WORKSPACES
-- =========================================================

INSERT INTO workspaces (id, name, description, created_by, deleted_at, created_at, updated_at)
VALUES
    (2001, 'G5 Demo Workspace', 'Main workspace for teacher demo: owner, PM, developers, and workspace-only member.', 1001, NULL, '2026-06-02 09:00:00', '2026-06-02 09:00:00'),
    (2002, 'PM Private Workspace', 'Workspace owned by the PM user to test workspace isolation.', 1002, NULL, '2026-06-02 10:00:00', '2026-06-02 10:00:00'),
    (2003, 'Deleted Workspace', 'Soft-deleted workspace that should not appear in normal workspace lists.', 1001, '2026-06-10 12:00:00', '2026-06-02 11:00:00', '2026-06-10 12:00:00');

INSERT INTO workspace_members (id, workspace_id, user_id, role, joined_at)
VALUES
    (20001, 2001, 1001, 'OWNER', '2026-06-02 09:00:00'),
    (20002, 2001, 1002, 'MEMBER', '2026-06-02 09:05:00'),
    (20003, 2001, 1003, 'MEMBER', '2026-06-02 09:10:00'),
    (20004, 2001, 1004, 'MEMBER', '2026-06-02 09:15:00'),
    (20005, 2001, 1005, 'MEMBER', '2026-06-02 09:20:00'),
    (20006, 2002, 1002, 'OWNER', '2026-06-02 10:00:00'),
    (20007, 2002, 1003, 'MEMBER', '2026-06-02 10:10:00'),
    (20008, 2003, 1001, 'OWNER', '2026-06-02 11:00:00');

-- =========================================================
-- 3. PROJECTS
-- =========================================================

INSERT INTO projects (id, workspace_id, name, description, start_date, end_date, created_by, deleted_at, created_at, updated_at)
VALUES
    (3001, 2001, 'Backend Core API', 'Core auth, workspace, project, member, and task workflow APIs.', '2026-06-03', '2026-07-15', 1001, NULL, '2026-06-03 08:00:00', '2026-06-03 08:00:00'),
    (3002, 2001, 'Frontend Integration', 'Project used to test members seeing only projects they belong to.', '2026-06-05', '2026-07-30', 1001, NULL, '2026-06-05 09:00:00', '2026-06-05 09:00:00'),
    (3003, 2001, 'Archived Mobile Prototype', 'Soft-deleted project that should not appear in normal project lists.', '2026-05-01', '2026-05-20', 1001, '2026-06-10 15:00:00', '2026-05-01 09:00:00', '2026-06-10 15:00:00'),
    (3004, 2002, 'PM Workspace Project', 'Project in another workspace to test isolation between workspaces.', '2026-06-06', '2026-06-30', 1002, NULL, '2026-06-06 09:00:00', '2026-06-06 09:00:00');

INSERT INTO project_members (id, workspace_id, project_id, user_id, role, joined_at)
VALUES
    (30001, 2001, 3001, 1002, 'PROJECT_MANAGER', '2026-06-03 08:05:00'),
    (30002, 2001, 3001, 1003, 'DEVELOPER', '2026-06-03 08:10:00'),
    (30003, 2001, 3001, 1004, 'DEVELOPER', '2026-06-03 08:15:00'),
    (30004, 2001, 3002, 1004, 'PROJECT_MANAGER', '2026-06-05 09:05:00'),
    (30005, 2001, 3002, 1003, 'DEVELOPER', '2026-06-05 09:10:00'),
    (30006, 2001, 3003, 1002, 'PROJECT_MANAGER', '2026-05-01 09:05:00'),
    (30007, 2001, 3003, 1003, 'DEVELOPER', '2026-05-01 09:10:00'),
    (30008, 2002, 3004, 1002, 'PROJECT_MANAGER', '2026-06-06 09:05:00'),
    (30009, 2002, 3004, 1003, 'DEVELOPER', '2026-06-06 09:10:00');

-- =========================================================
-- 4. WORK ITEMS
-- =========================================================

INSERT INTO work_items (
    id, workspace_id, project_id, title, description, type, status, priority,
    due_date, assignee_id, created_by, related_work_item_id, deleted_at, created_at, updated_at
)
VALUES
    (4001, 2001, 3001, 'Implement login API', 'TODO task assigned to Developer One. Login as dev1.demo@g5.local and call PATCH /api/tasks/4001/start.', 'TASK', 'TODO', 'HIGH', '2026-06-20', 1003, 1002, NULL, NULL, '2026-06-03 09:00:00', '2026-06-03 09:00:00'),
    (4002, 2001, 3001, 'Build workspace list API', 'IN_PROGRESS task assigned to Developer One. Login as dev1 and call PATCH /api/tasks/4002/submit-review.', 'TASK', 'IN_PROGRESS', 'MEDIUM', '2026-06-22', 1003, 1002, NULL, NULL, '2026-06-03 10:00:00', '2026-06-04 08:00:00'),
    (4003, 2001, 3001, 'Review project permission rules', 'REVIEW task. Login as PM or Owner and call approve, request-changes, or cancel.', 'TASK', 'REVIEW', 'URGENT', '2026-06-18', 1003, 1002, NULL, NULL, '2026-06-03 11:00:00', '2026-06-05 14:00:00'),
    (4004, 2001, 3001, 'Draft task board filters', 'TODO task without assignee. Starting this task should fail until an assignee is set.', 'TASK', 'TODO', 'LOW', '2026-06-25', NULL, 1002, NULL, NULL, '2026-06-03 12:00:00', '2026-06-03 12:00:00'),
    (4005, 2001, 3001, 'Finalize JWT refresh flow', 'DONE task used to test final-state update and transition rejection.', 'TASK', 'DONE', 'HIGH', '2026-06-12', 1004, 1002, NULL, NULL, '2026-06-03 13:00:00', '2026-06-09 16:00:00'),
    (4006, 2001, 3001, 'Remove obsolete prototype endpoint', 'CANCELLED task used to test final-state update and transition rejection.', 'TASK', 'CANCELLED', 'MEDIUM', '2026-06-15', 1004, 1002, NULL, NULL, '2026-06-03 14:00:00', '2026-06-08 10:00:00'),
    (4007, 2001, 3001, 'Fix Swagger bearer authorization docs', 'REVIEW task assigned to Developer Two. Good for testing assignee-specific restrictions.', 'TASK', 'REVIEW', 'MEDIUM', '2026-06-19', 1004, 1002, NULL, NULL, '2026-06-04 09:00:00', '2026-06-07 10:00:00'),
    (4008, 2001, 3001, 'Login keyword search sample', 'Use keyword=login, priority=URGENT, or assigneeId=1004 when testing GET /api/projects/3001/tasks.', 'TASK', 'TODO', 'URGENT', '2026-06-28', 1004, 1002, 4001, NULL, '2026-06-04 10:00:00', '2026-06-04 10:00:00'),
    (4009, 2001, 3001, 'Hidden soft-deleted task', 'This row has deleted_at set and should not appear in task list/detail APIs.', 'TASK', 'TODO', 'LOW', '2026-06-30', 1003, 1002, NULL, '2026-06-10 10:00:00', '2026-06-04 11:00:00', '2026-06-10 10:00:00'),
    (4010, 2001, 3002, 'Connect frontend board to API', 'Task in project 3002. Developer One can view; PM user from project 3001 should not manage this project unless workspace owner.', 'TASK', 'TODO', 'HIGH', '2026-07-05', 1003, 1004, NULL, NULL, '2026-06-05 10:00:00', '2026-06-05 10:00:00'),
    (4011, 2002, 3004, 'Private workspace task', 'Task in workspace 2002 to prove users outside that workspace cannot access it.', 'TASK', 'TODO', 'MEDIUM', '2026-06-26', 1003, 1002, NULL, NULL, '2026-06-06 10:00:00', '2026-06-06 10:00:00');

-- =========================================================
-- 5. OPTIONAL V2/V3 SAMPLE DATA
-- =========================================================

INSERT INTO work_item_comments (id, work_item_id, user_id, content, deleted_at, created_at, updated_at)
VALUES
    (5001, 4003, 1003, 'Ready for review. Please check the permission edge cases.', NULL, '2026-06-05 13:30:00', '2026-06-05 13:30:00'),
    (5002, 4005, 1002, 'Approved during the first review pass.', NULL, '2026-06-09 16:05:00', '2026-06-09 16:05:00');

INSERT INTO work_item_attachments (id, work_item_id, uploaded_by, original_file_name, stored_file_name, file_path, file_size, content_type, deleted_at, created_at)
VALUES
    (6001, 4003, 1003, 'permission-test-notes.txt', '4003-permission-test-notes.txt', '/demo/uploads/4003-permission-test-notes.txt', 2048, 'text/plain', NULL, '2026-06-05 13:40:00');

INSERT INTO work_item_activity_logs (id, work_item_id, actor_id, action, old_value, new_value, created_at)
VALUES
    (7001, 4001, 1002, 'CREATE_TASK', NULL, 'TODO', '2026-06-03 09:00:00'),
    (7002, 4002, 1003, 'START_TASK', 'TODO', 'IN_PROGRESS', '2026-06-04 08:00:00'),
    (7003, 4003, 1003, 'SUBMIT_REVIEW', 'IN_PROGRESS', 'REVIEW', '2026-06-05 14:00:00'),
    (7004, 4005, 1002, 'APPROVE_TASK', 'REVIEW', 'DONE', '2026-06-09 16:00:00');

INSERT INTO workspace_invitations (
    id, workspace_id, email, role, token_hash, status, invited_by, accepted_by,
    expires_at, accepted_at, cancelled_at, created_at
)
VALUES
    (8001, 2001, 'new.member@g5.local', 'MEMBER', 'demo-pending-token-hash', 'PENDING', 1001, NULL, '2026-07-01 00:00:00', NULL, NULL, '2026-06-08 09:00:00'),
    (8002, 2001, 'accepted.member@g5.local', 'MEMBER', 'demo-accepted-token-hash', 'ACCEPTED', 1001, 1005, '2026-07-01 00:00:00', '2026-06-09 09:00:00', NULL, '2026-06-08 09:10:00'),
    (8003, 2001, 'cancelled.member@g5.local', 'MEMBER', 'demo-cancelled-token-hash', 'CANCELLED', 1001, NULL, '2026-07-01 00:00:00', NULL, '2026-06-09 10:00:00', '2026-06-08 09:20:00');

INSERT INTO notifications (
    id, recipient_id, actor_id, workspace_id, project_id, work_item_id,
    type, title, content, is_read, read_at, created_at
)
VALUES
    (9001, 1003, 1002, 2001, 3001, 4001, 'TASK_ASSIGNED', 'New task assigned', 'You were assigned: Implement login API.', FALSE, NULL, '2026-06-03 09:01:00'),
    (9002, 1002, 1003, 2001, 3001, 4003, 'TASK_SUBMITTED', 'Task submitted for review', 'Developer One submitted the permission rules task.', TRUE, '2026-06-05 14:10:00', '2026-06-05 14:01:00');

-- Keep future auto-generated IDs away from the fixed demo range.
ALTER TABLE users AUTO_INCREMENT = 1100;
ALTER TABLE workspaces AUTO_INCREMENT = 2100;
ALTER TABLE workspace_members AUTO_INCREMENT = 20100;
ALTER TABLE projects AUTO_INCREMENT = 3100;
ALTER TABLE project_members AUTO_INCREMENT = 30100;
ALTER TABLE work_items AUTO_INCREMENT = 4100;
ALTER TABLE work_item_comments AUTO_INCREMENT = 5100;
ALTER TABLE work_item_attachments AUTO_INCREMENT = 6100;
ALTER TABLE work_item_activity_logs AUTO_INCREMENT = 7100;
ALTER TABLE workspace_invitations AUTO_INCREMENT = 8100;
ALTER TABLE notifications AUTO_INCREMENT = 9100;
