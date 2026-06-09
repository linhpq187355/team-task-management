CREATE DATABASE IF NOT EXISTS team_task_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE team_task_management;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notifications;

DROP TABLE IF EXISTS workspace_invitations;

DROP TABLE IF EXISTS work_item_attachments;

DROP TABLE IF EXISTS work_item_comments;

DROP TABLE IF EXISTS work_item_activity_logs;

DROP TABLE IF EXISTS work_items;

DROP TABLE IF EXISTS project_members;

DROP TABLE IF EXISTS projects;

DROP TABLE IF EXISTS workspace_members;

DROP TABLE IF EXISTS workspaces;

DROP TABLE IF EXISTS refresh_tokens;

DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 1. USERS
-- =========================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500) NULL,
    status ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE = InnoDB;

-- =========================================================
-- 2. REFRESH TOKENS
-- =========================================================

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_refresh_tokens_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- =========================================================
-- 3. WORKSPACES
-- =========================================================

CREATE TABLE workspaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    created_by BIGINT NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workspaces_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- =========================================================
-- 4. WORKSPACE MEMBERS
-- =========================================================

CREATE TABLE workspace_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('OWNER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workspace_members_workspace_user UNIQUE (workspace_id, user_id),
    CONSTRAINT fk_workspace_members_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_workspace_members_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- =========================================================
-- 5. PROJECTS
-- =========================================================

CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    created_by BIGINT NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_projects_id_workspace UNIQUE (id, workspace_id),
    CONSTRAINT fk_projects_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_projects_date_range CHECK (
        start_date IS NULL
        OR end_date IS NULL
        OR start_date <= end_date
    )
) ENGINE = InnoDB;

-- =========================================================
-- 6. PROJECT MEMBERS
-- =========================================================
-- workspace_id được lưu thêm để DB có thể kiểm tra:
-- user muốn vào project thì bắt buộc phải là member của workspace trước.

CREATE TABLE project_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM(
        'PROJECT_MANAGER',
        'DEVELOPER'
    ) NOT NULL,
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
\    CONSTRAINT uq_project_members_project_user_role UNIQUE (project_id, user_id, role),
    CONSTRAINT fk_project_members_project_workspace FOREIGN KEY (project_id, workspace_id) REFERENCES projects (id, workspace_id) ON DELETE CASCADE,
    CONSTRAINT fk_project_members_workspace_member FOREIGN KEY (workspace_id, user_id) REFERENCES workspace_members (workspace_id, user_id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- =========================================================
-- 7. WORK ITEMS
-- =========================================================
-- V1 chỉ dùng type = 'TASK'
-- Sau này có thể dùng type = 'BUG' mà không cần đổi schema.

CREATE TABLE work_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NULL,
    type ENUM('TASK', 'BUG') NOT NULL DEFAULT 'TASK',
    status ENUM(
        'TODO',
        'IN_PROGRESS',
        'REVIEW',
        'DONE',
        'CANCELLED'
    ) NOT NULL DEFAULT 'TODO',
    priority ENUM(
        'LOW',
        'MEDIUM',
        'HIGH',
        'URGENT'
    ) NOT NULL DEFAULT 'MEDIUM',
    due_date DATE NULL,
    assignee_id BIGINT NULL,
    created_by BIGINT NOT NULL,
    related_work_item_id BIGINT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_items_project_workspace FOREIGN KEY (project_id, workspace_id) REFERENCES projects (id, workspace_id) ON DELETE CASCADE,
    CONSTRAINT fk_work_items_assignee FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL,    CONSTRAINT fk_work_items_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_work_items_related FOREIGN KEY (related_work_item_id) REFERENCES work_items (id) ON DELETE SET NULL
) ENGINE = InnoDB;

-- =========================================================
-- 8. WORK ITEM COMMENTS - V2
-- =========================================================

CREATE TABLE work_item_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_item_comments_work_item FOREIGN KEY (work_item_id) REFERENCES work_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_work_item_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- =========================================================
-- 9. WORK ITEM ATTACHMENTS - V2
-- =========================================================

CREATE TABLE work_item_attachments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_item_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NULL,
    deleted_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_item_attachments_work_item FOREIGN KEY (work_item_id) REFERENCES work_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_work_item_attachments_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_work_item_attachments_file_size CHECK (
        file_size > 0
        AND file_size <= 10485760
    )
) ENGINE = InnoDB;

-- =========================================================
-- 10. WORK ITEM ACTIVITY LOGS - V2
-- =========================================================
-- Bảng log chỉ insert, không update.
-- Không cần updated_at.

CREATE TABLE work_item_activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_item_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    old_value TEXT NULL,
    new_value TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_item_activity_logs_work_item FOREIGN KEY (work_item_id) REFERENCES work_items (id) ON DELETE CASCADE,
    CONSTRAINT fk_work_item_activity_logs_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE RESTRICT
) ENGINE = InnoDB;

-- =========================================================
-- 11. WORKSPACE INVITATIONS - V2
-- =========================================================
-- V1 chưa dùng.
-- V2 dùng khi làm email invitation thật.

CREATE TABLE workspace_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workspace_id BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL,
    role ENUM('OWNER', 'MEMBER') NOT NULL DEFAULT 'MEMBER',
    token_hash VARCHAR(255) NOT NULL,
    status ENUM(
        'PENDING',
        'ACCEPTED',
        'EXPIRED',
        'CANCELLED'
    ) NOT NULL DEFAULT 'PENDING',
    invited_by BIGINT NOT NULL,
    accepted_by BIGINT NULL,
    expires_at DATETIME NOT NULL,
    accepted_at DATETIME NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_workspace_invitations_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_workspace_invitations_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_workspace_invitations_invited_by FOREIGN KEY (invited_by) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_workspace_invitations_accepted_by FOREIGN KEY (accepted_by) REFERENCES users (id) ON DELETE SET NULL
) ENGINE = InnoDB;

-- =========================================================
-- 12. NOTIFICATIONS - V3
-- =========================================================

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NULL,
    workspace_id BIGINT NULL,
    project_id BIGINT NULL,
    work_item_id BIGINT NULL,
    type VARCHAR(100) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_project FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_work_item FOREIGN KEY (work_item_id) REFERENCES work_items (id) ON DELETE CASCADE
) ENGINE = InnoDB;

-- =========================================================
-- 13. INDEXES
-- =========================================================

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE INDEX idx_workspaces_created_by ON workspaces (created_by);

CREATE INDEX idx_workspace_members_user_id ON workspace_members (user_id);

CREATE INDEX idx_workspace_members_workspace_id ON workspace_members (workspace_id);

CREATE INDEX idx_projects_workspace_id ON projects (workspace_id);

CREATE INDEX idx_projects_created_by ON projects (created_by);

CREATE INDEX idx_project_members_workspace_id ON project_members (workspace_id);

CREATE INDEX idx_project_members_project_id ON project_members (project_id);

CREATE INDEX idx_project_members_user_id ON project_members (user_id);

CREATE INDEX idx_project_members_role ON project_members (role);

CREATE INDEX idx_work_items_workspace_id ON work_items (workspace_id);

CREATE INDEX idx_work_items_project_id ON work_items (project_id);

CREATE INDEX idx_work_items_assignee_id ON work_items (assignee_id);

CREATE INDEX idx_work_items_created_by ON work_items (created_by);

CREATE INDEX idx_work_items_type
ON work_items(type);

CREATE INDEX idx_work_items_status ON work_items (status);

CREATE INDEX idx_work_items_priority ON work_items (priority);

CREATE INDEX idx_work_items_due_date ON work_items (due_date);

CREATE INDEX idx_work_items_project_type_status
ON work_items(project_id, type, status);

CREATE INDEX idx_work_items_project_assignee ON work_items (project_id, assignee_id);

CREATE INDEX idx_work_item_comments_work_item_id ON work_item_comments (work_item_id);

CREATE INDEX idx_work_item_comments_user_id ON work_item_comments (user_id);

CREATE INDEX idx_work_item_attachments_work_item_id ON work_item_attachments (work_item_id);

CREATE INDEX idx_work_item_attachments_uploaded_by ON work_item_attachments (uploaded_by);

CREATE INDEX idx_work_item_activity_logs_work_item_id ON work_item_activity_logs (work_item_id);

CREATE INDEX idx_work_item_activity_logs_actor_id ON work_item_activity_logs (actor_id);

CREATE INDEX idx_workspace_invitations_workspace_id ON workspace_invitations (workspace_id);

CREATE INDEX idx_workspace_invitations_email ON workspace_invitations (email);

CREATE INDEX idx_workspace_invitations_status ON workspace_invitations (status);

CREATE INDEX idx_notifications_recipient_id ON notifications (recipient_id);

CREATE INDEX idx_notifications_recipient_read ON notifications (recipient_id, is_read);

CREATE INDEX idx_notifications_work_item_id ON notifications (work_item_id);

CREATE INDEX idx_notifications_created_at ON notifications (created_at);