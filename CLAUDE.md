# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

Uses the Maven wrapper (`./mvnw`, or `mvnw.cmd` on Windows). Java 17, Spring Boot 4.0.6.

```bash
./mvnw clean package             # build the runnable jar (target/team-task-management-0.0.1-SNAPSHOT.jar)
./mvnw spring-boot:run           # run the app locally on :8080 (needs MySQL up)
./mvnw test                      # run all tests
./mvnw test -Dtest=WorkItemServiceTest              # single test class
./mvnw test -Dtest=WorkItemServiceTest#methodName   # single test method
```

Tests run against an in-memory H2 database (`src/test/resources/application.properties`, MySQL-compatibility mode) — no MySQL needed for tests.

### Local dev database

MySQL runs in Docker, the app runs on the host (fast rebuilds, easy debug):

```bash
docker compose up -d mysql db-init   # MySQL on host port 3307, runs docker/mysql/init/*.sql
docker compose down                  # stop   (add -v to wipe the volume/database)
```

DB connection is env-driven (see `.env`, defaults in `application.properties`): host `localhost`, port `3307`, db/user/pass `team_task_management` / `teamtask` / `teamtask`. Swagger UI at http://localhost:8080/swagger-ui.html, API docs at `/v3/api-docs`. See `DOCKER_SETUP.md` (Vietnamese) for the full workflow.

## Architecture

REST backend for a team task manager. Layered: **Controller → Service (interface + `impl/`) → Repository (Spring Data JPA) → Entity**. Package root `com.g5.teamtaskmanagement`. Entities are never returned from controllers — request/response DTOs in `dto/request` and `dto/response` cross the boundary.

`ai-agents.md` is the authoritative business-rules spec (Vietnamese) — read it before touching authorization, work-item workflow, or validation logic. Key points below.

### Two-level authorization model (the core of this system)

All permission checks live in `PermissionService` / `PermissionServiceImpl` — route authorization decisions through it, don't re-derive roles inline in each service.

- **Workspace roles** (`workspace_members`): `OWNER` / `MEMBER`.
- **Project roles** (`project_members`): `PROJECT_MANAGER` / `DEVELOPER`.
- **Inheritance rule (critical):** a Workspace `OWNER` implicitly has Project Manager rights on *every* project in that workspace, even without a `project_members` row. `PermissionService` methods like `canManageProject` / `canManageTask` already bake this in — always bypass the PM check when the user is the workspace owner.

Auth is stateless JWT (`security/` package: `JwtAuthenticationFilter`, `JwtService`, `CustomUserDetails*`). `SecurityConfig` permits `/api/auth/**` and Swagger; everything else requires a valid bearer token. Fetch the caller via `CurrentUserService.getCurrentUser()` / `getCurrentUserId()` rather than reading the SecurityContext directly.

### Work-item state machine

`work_items` (`type` = `TASK` in V1; `BUG` reserved for later). Status transitions are strictly enforced — any transition not in the table below must throw `BadRequestException`:

```
TODO ─────► IN_PROGRESS   (Start, by Assignee; requires assignee_id set)
TODO ─────► CANCELLED      (by PM / Workspace Owner)
IN_PROGRESS ─► REVIEW      (Submit, by Assignee)
IN_PROGRESS ─► CANCELLED   (by PM / Workspace Owner)
REVIEW ───► DONE           (Approve, by PM / Workspace Owner)
REVIEW ───► IN_PROGRESS    (Request changes, by PM / Workspace Owner)
REVIEW ───► CANCELLED      (by PM / Workspace Owner)
DONE, CANCELLED = final, no transitions out
```

Task edit rules that are easy to miss: only PM/Owner create tasks; `due_date` compared by DATE (not time) must be `>= created date`, including on update; max one assignee, who must be a project `DEVELOPER`; once `DONE`/`CANCELLED`, core fields (`title`, `description`, `priority`, `due_date`, `assignee_id`) are immutable.

### Soft delete

`workspaces` and `projects` use `deleted_at` — list APIs must exclude soft-deleted rows and cascade the hide down (workspace hidden → its projects & work items hidden). `work_items` have **no** soft delete; use the `CANCELLED` status instead. Timestamp/audit columns come from the `CreatedAtEntity` / `TimestampedEntity` base classes.

### Conventions

- Responses wrap in `ApiResponse<T>` (`success` / `message` / `data`, or `errorCode` + `errors` on failure) via its static `success(...)` / `failure(...)` factories.
- Validate DTOs with `@Valid` + Jakarta constraints at the controller.
- Exceptions are handled centrally in `exception/GlobalExceptionHandler` — throw the domain exceptions (`ResourceNotFoundException`, `BadRequestException`, `ForbiddenException`, `UnauthorizedException`, `DuplicateResourceException`) rather than building error responses in services.
- Date JSON format: `yyyy-MM-dd` or `yyyy-MM-dd HH:mm:ss`.
- Lombok is used; annotation processing is wired in the compiler plugin.

### Scope note

Some entities exist for future versions (`Notification`, `WorkItemComment`, `WorkItemAttachment`, `WorkItemActivityLog`, `WorkspaceInvitation`). Per `ai-agents.md`, V1 is the core system — don't build out v2/v3 features (comments, attachments, notifications, kanban drag-drop) unless explicitly asked.
