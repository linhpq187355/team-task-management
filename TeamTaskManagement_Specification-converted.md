Dựa trên bản **v1.1 Revised**, backend nên làm theo hướng: **xong Core v1 trước**, có API đủ cho frontend chạy luồng chính: đăng ký → login → workspace → project → member → task board → đổi trạng thái task. Các phần comment, attachment, dashboard, realtime để sau vì tài liệu cũng đã tách chúng sang v2/v3.

## Mục tiêu backend cần đạt trước khi giao cho frontend

Frontend cần có API để làm được các màn hình chính sau:

1. Login / Register
2. Profile
3. Workspace List / Workspace Detail
4. Project Detail
5. Project Member Management
6. Task Board
7. Task Detail
8. Task status action: Start / Submit Review / Approve / Request Changes / Cancel

Vì vậy backend nên chia nhỏ theo thứ tự này.

# Giai đoạn 0 — Khởi tạo project backend

## 0.1. Tạo Spring Boot project

Công việc:

* Tạo project Spring Boot 3
* Java 17
* Maven
* MySQL
* Dependencies:
  + Spring Web
  + Spring Data JPA
  + MySQL Driver
  + Spring Security
  + Validation
  + Lombok
  + Springdoc OpenAPI / Swagger
  + JWT library
  + DevTools nếu cần

## 0.2. Tạo cấu trúc package

Nên chia như này:

com.teamtaskmanagement

├── auth

├── user

├── workspace

├── project

├── task

├── security

├── common

└── config

Trong đó:

common

├── dto

├── exception

├── response

├── pagination

└── enums

## 0.3. Cấu hình cơ bản

Cần làm:

* application.yml
* Kết nối MySQL
* Cấu hình JPA/Hibernate
* Cấu hình CORS cho frontend
* Cấu hình Swagger
* Cấu hình global exception handler

Kết quả cần có:

* Chạy được backend
* Kết nối được DB
* Mở được Swagger UI
* Có response lỗi chuẩn JSON

# Giai đoạn 1 — Thiết kế database core v1

Theo bản revised, v1 chỉ cần các bảng core: users, refresh\_tokens, workspaces, workspace\_members, projects, project\_members, tasks. Các bảng comment, attachment, activity để v2.

## 1.1. Tạo enum

Cần tạo trước:

WorkspaceRole {

OWNER,

MEMBER

}

ProjectRole {

PROJECT\_MANAGER,

DEVELOPER

}

TaskStatus {

TODO,

IN\_PROGRESS,

REVIEW,

DONE,

CANCELLED

}

TaskPriority {

LOW,

MEDIUM,

HIGH,

URGENT

}

## 1.2. Tạo entity core

Làm theo thứ tự:

1. User
2. RefreshToken
3. Workspace
4. WorkspaceMember
5. Project
6. ProjectMember
7. Task

## 1.3. Base entity

Nên có class dùng chung:

createdAt

updatedAt

Với Workspace và Project thêm:

deletedAt

Task v1 **chưa cần soft delete**, vì bản revised đã chốt Task v1 không hỗ trợ soft delete để tránh nhầm với trạng thái CANCELLED.

# Giai đoạn 2 — Common backend foundation

## 2.1. Response format

Tạo response chuẩn:

{

"success": true,

"message": "Success",

"data": {}

}

Với lỗi:

{

"success": false,

"message": "Workspace not found",

"errors": []

}

## 2.2. Exception handler

Cần có:

* ResourceNotFoundException
* BadRequestException
* UnauthorizedException
* ForbiddenException
* DuplicateResourceException
* GlobalExceptionHandler

## 2.3. Validation

Dùng annotation:

@NotBlank

@NotNull

@Email

@Size

@FutureOrPresent

# Giai đoạn 3 — Auth & User API

Đây là phần nên làm đầu tiên vì các API khác đều cần JWT.

## 3.1. Register

Endpoint:

POST /api/auth/register

Request:

{

"email": "user@gmail.com",

"password": "123456",

"fullName": "Nguyen Van A"

}

Cần xử lý:

* Check email chưa tồn tại
* Hash password bằng BCrypt
* Tạo user
* Trả về thông tin user cơ bản

## 3.2. Login

Endpoint:

POST /api/auth/login

Response:

{

"accessToken": "...",

"refreshToken": "...",

"user": {

"id": 1,

"email": "user@gmail.com",

"fullName": "Nguyen Van A"

}

}

Cần xử lý:

* Xác thực email/password
* Sinh access token
* Sinh refresh token
* Lưu refresh token vào DB

## 3.3. Refresh token

Endpoint:

POST /api/auth/refresh

Cần xử lý:

* Kiểm tra refresh token còn hạn
* Sinh access token mới

## 3.4. Logout

Endpoint:

POST /api/auth/logout

Cần xử lý:

* Xóa hoặc revoke refresh token

## 3.5. Profile

Endpoints:

GET /api/users/me

PUT /api/users/me

PUT /api/users/me/password

Kết quả giai đoạn này:

* Frontend đăng ký được
* Đăng nhập được
* Gửi JWT để gọi API protected được
* Xem/cập nhật profile được

# Giai đoạn 4 — Security & Authorization nền tảng

Phần này rất quan trọng vì project của bạn có phân quyền 2 cấp: Workspace Role và Project Role. Workspace Owner có quyền cao nhất trong workspace và được phép vượt qua quyền Project Manager.

## 4.1. JWT filter

Cần làm:

* JwtTokenProvider
* JwtAuthenticationFilter
* CustomUserDetails
* CustomUserDetailsService
* SecurityConfig

## 4.2. Current user helper

Tạo helper:

CurrentUserService.getCurrentUser()

CurrentUserService.getCurrentUserId()

Để service không phải lặp code lấy user từ SecurityContext.

## 4.3. Permission service

Tạo class riêng:

PermissionService

Các method cần có:

boolean isWorkspaceOwner(Long workspaceId, Long userId)

boolean isWorkspaceMember(Long workspaceId, Long userId)

boolean isProjectMember(Long projectId, Long userId)

boolean isProjectManager(Long projectId, Long userId)

boolean isWorkspaceOwnerOfProject(Long projectId, Long userId)

boolean canManageProject(Long projectId, Long userId)

boolean canViewProject(Long projectId, Long userId)

boolean canManageTask(Long taskId, Long userId)

boolean isTaskAssignee(Long taskId, Long userId)

Trong đó:

canManageProject = Project Manager OR Workspace Owner

canManageTask = Project Manager OR Workspace Owner

# Giai đoạn 5 — Workspace API

## 5.1. Tạo workspace

Endpoint:

POST /api/workspaces

Request:

{

"name": "G5 Workspace",

"description": "Workspace for team project"

}

Cần xử lý:

* Tạo workspace
* User hiện tại tự động thành OWNER
* Tạo record trong workspace\_members

## 5.2. Lấy danh sách workspace của user

Endpoint:

GET /api/workspaces

Trả về những workspace mà user đang là member.

## 5.3. Lấy chi tiết workspace

Endpoint:

GET /api/workspaces/{id}

Chỉ workspace member mới xem được.

## 5.4. Cập nhật workspace

Endpoint:

PUT /api/workspaces/{id}

Chỉ Workspace Owner.

## 5.5. Xóa mềm workspace

Endpoint:

DELETE /api/workspaces/{id}

Chỉ Workspace Owner.

Xử lý:

deletedAt = now()

Không cần xóa thật DB.

## 5.6. Quản lý workspace member

Endpoints:

GET /api/workspaces/{id}/members

POST /api/workspaces/{id}/members

PUT /api/workspaces/{id}/members/{userId}/role

DELETE /api/workspaces/{id}/members/{userId}

Business rules cần nhớ:

* V1 chỉ thêm thành viên bằng email user đã tồn tại.
* Chỉ Workspace Owner được thêm/xóa/đổi role.
* Không cho xóa Owner duy nhất.
* User bị xóa khỏi workspace thì mất quyền xem project/task trong workspace.

Kết quả giai đoạn này:

* Frontend làm được màn Workspace List
* Workspace Detail
* Add/remove member

# Giai đoạn 6 — Project API

## 6.1. Tạo project

Endpoint:

POST /api/workspaces/{workspaceId}/projects

Request:

{

"name": "Team Task Management",

"description": "Graduation project",

"startDate": "2026-06-10",

"endDate": "2026-07-10"

}

Cần xử lý:

* Chỉ Workspace Owner được tạo project theo bản v1.1.
* Project thuộc workspace.
* startDate <= endDate.
* Người tạo project tự động thành PROJECT\_MANAGER.

## 6.2. Lấy danh sách project trong workspace

Endpoint:

GET /api/workspaces/{workspaceId}/projects

Quyền:

* Workspace Owner xem được tất cả.
* Workspace Member chỉ nên xem project mà họ là project member.

## 6.3. Lấy chi tiết project

Endpoint:

GET /api/projects/{id}

Quyền:

* Workspace Owner
* Project Member

## 6.4. Cập nhật project

Endpoint:

PUT /api/projects/{id}

Quyền:

* Project Manager
* Workspace Owner

## 6.5. Xóa mềm project

Endpoint:

DELETE /api/projects/{id}

Quyền:

* Project Manager
* Workspace Owner

Xử lý:

deletedAt = now()

## 6.6. Quản lý project member

Endpoints:

GET /api/projects/{id}/members

POST /api/projects/{id}/members

PUT /api/projects/{id}/members/{userId}/role

DELETE /api/projects/{id}/members/{userId}

Business rules:

* Chỉ thêm user đã thuộc workspace vào project.
* Một user chỉ có một role trong một project.
* Role chỉ là PROJECT\_MANAGER hoặc DEVELOPER.
* Chỉ Developer mới được assign task.

Kết quả giai đoạn này:

* Frontend tạo được project
* Xem project detail
* Thêm thành viên vào project
* Gán PM / Developer

# Giai đoạn 7 — Task API core

Đây là phần quan trọng nhất để có API cho frontend Task Board.

## 7.1. Tạo task

Endpoint:

POST /api/projects/{projectId}/tasks

Request:

{

"title": "Implement login API",

"description": "Create login endpoint with JWT",

"priority": "HIGH",

"dueDate": "2026-06-15",

"assigneeId": 3

}

Cần xử lý:

* Chỉ Project Manager hoặc Workspace Owner được tạo task.
* Status mặc định là TODO.
* Assignee có thể null.
* Nếu có assignee thì assignee phải là Project Member role DEVELOPER.
* dueDate >= createdAt.
* Task v1 chưa cần soft delete.

## 7.2. Lấy danh sách task trong project

Endpoint:

GET /api/projects/{projectId}/tasks

Nên hỗ trợ query:

GET /api/projects/1/tasks?status=TODO&priority=HIGH&assigneeId=3&keyword=login&page=0&size=10&sort=dueDate,asc

Filter nên có:

* status
* priority
* assigneeId
* keyword
* page
* size
* sort

Frontend Task Board cần API này để render các cột Kanban.

## 7.3. Lấy chi tiết task

Endpoint:

GET /api/tasks/{id}

Quyền:

* Workspace Owner
* Project Member

## 7.4. Cập nhật task

Endpoint:

PUT /api/tasks/{id}

Request:

{

"title": "Implement login API",

"description": "Create login endpoint with JWT and refresh token",

"priority": "URGENT",

"dueDate": "2026-06-16",

"assigneeId": 4

}

Business rules:

* Chỉ PM hoặc Workspace Owner.
* Không cho sửa nếu task đã DONE hoặc CANCELLED.
* Nếu đổi assignee thì assignee mới phải là Developer trong project.
* Nếu đổi dueDate thì không được nhỏ hơn ngày tạo task.

Bản revised cũng ghi rõ không cho chỉnh sửa title, description, priority, due\_date, assignee nếu task đã ở final state DONE hoặc CANCELLED.

# Giai đoạn 8 — Task Status Workflow API

Đây là phần nên tách riêng, không gộp vào update task thường.

Endpoint:

PATCH /api/tasks/{id}/status

Request:

{

"targetStatus": "IN\_PROGRESS"

}

Hoặc tốt hơn cho frontend:

PATCH /api/tasks/{id}/start

PATCH /api/tasks/{id}/submit-review

PATCH /api/tasks/{id}/approve

PATCH /api/tasks/{id}/request-changes

PATCH /api/tasks/{id}/cancel

Với portfolio backend, cách thứ hai dễ thể hiện nghiệp vụ hơn.

## 8.1. Start Task

PATCH /api/tasks/{id}/start

Rule:

* Current status phải là TODO.
* Chỉ assignee được bấm.
* Task phải có assignee.
* Chuyển sang IN\_PROGRESS.

## 8.2. Submit for Review

PATCH /api/tasks/{id}/submit-review

Rule:

* Current status phải là IN\_PROGRESS.
* Chỉ assignee được bấm.
* Chuyển sang REVIEW.

## 8.3. Approve / Mark as Done

PATCH /api/tasks/{id}/approve

Rule:

* Current status phải là REVIEW.
* Chỉ PM hoặc Workspace Owner.
* Chuyển sang DONE.

## 8.4. Request Changes

PATCH /api/tasks/{id}/request-changes

Rule:

* Current status phải là REVIEW.
* Chỉ PM hoặc Workspace Owner.
* Chuyển về IN\_PROGRESS.

## 8.5. Cancel Task

PATCH /api/tasks/{id}/cancel

Rule:

* Cho phép từ TODO, IN\_PROGRESS, REVIEW.
* Chỉ PM hoặc Workspace Owner.
* Chuyển sang CANCELLED.

## 8.6. Final state rule

Không cho chuyển từ:

DONE -> bất kỳ trạng thái nào

CANCELLED -> bất kỳ trạng thái nào

Trong tài liệu, DONE và CANCELLED là trạng thái đóng, không cho chuyển ngược. Nếu task DONE phát sinh lỗi thì tạo task mới, không mở lại task cũ.

Kết quả giai đoạn này:

* Frontend bấm nút đổi trạng thái được
* Kanban board hoạt động đúng workflow
* Có đủ logic nghiệp vụ để bảo vệ/phỏng vấn

# Giai đoạn 9 — API response cho frontend

Sau khi có API core, cần chuẩn hóa DTO để frontend dễ dùng.

## 9.1. Auth response

{

"accessToken": "...",

"refreshToken": "...",

"user": {

"id": 1,

"email": "user@gmail.com",

"fullName": "Nguyen Van A"

}

}

## 9.2. Workspace response

{

"id": 1,

"name": "G5 Workspace",

"description": "Workspace for team project",

"myRole": "OWNER",

"createdAt": "2026-06-10T10:00:00"

}

## 9.3. Project response

{

"id": 1,

"workspaceId": 1,

"name": "Team Task Management",

"description": "Graduation project",

"myRole": "PROJECT\_MANAGER",

"startDate": "2026-06-10",

"endDate": "2026-07-10"

}

## 9.4. Task card response

Dùng cho Kanban:

{

"id": 1,

"title": "Implement login API",

"status": "TODO",

"priority": "HIGH",

"dueDate": "2026-06-15",

"assignee": {

"id": 3,

"fullName": "Nguyen Van B"

}

}

## 9.5. Task detail response

{

"id": 1,

"title": "Implement login API",

"description": "Create login endpoint with JWT",

"status": "IN\_PROGRESS",

"priority": "HIGH",

"dueDate": "2026-06-15",

"projectId": 1,

"assignee": {

"id": 3,

"fullName": "Nguyen Van B"

},

"createdBy": {

"id": 2,

"fullName": "Project Manager"

},

"createdAt": "2026-06-10T10:00:00",

"updatedAt": "2026-06-10T11:00:00"

}

# Giai đoạn 10 — Swagger + Postman test

## 10.1. Swagger

Cần đảm bảo tất cả API có:

* Endpoint rõ ràng
* Request body mẫu
* Response body mẫu
* JWT Authorization
* Mô tả quyền hạn

## 10.2. Postman collection

Tạo collection theo flow:

1. Register user A
2. Register user B
3. Login user A
4. Login user B
5. User A tạo workspace
6. User A thêm user B vào workspace
7. User A tạo project
8. User A thêm user B vào project role Developer
9. User A tạo task assign cho user B
10. User B start task
11. User B submit review
12. User A approve task

Đây chính là flow demo backend end-to-end.

# Giai đoạn 11 — Test nghiệp vụ quan trọng

Không cần test quá nhiều ngay từ đầu, nhưng nên có test cho service quan trọng.

## 11.1. Auth test

Test:

* Register thành công
* Register trùng email
* Login sai password
* Login đúng trả token

## 11.2. Workspace test

Test:

* Tạo workspace thì user thành Owner
* Member thường không update được workspace
* Không xóa Owner duy nhất

## 11.3. Project test

Test:

* Owner tạo project thành công
* Người tạo project thành Project Manager
* Không thêm user ngoài workspace vào project

## 11.4. Task test

Test:

* PM tạo task thành công
* Developer không được tạo task
* Không assign task cho user không phải Developer
* Không update task DONE/CANCELLED

## 11.5. Workflow test

Test:

* TODO → IN\_PROGRESS hợp lệ
* IN\_PROGRESS → REVIEW hợp lệ
* REVIEW → DONE hợp lệ
* REVIEW → IN\_PROGRESS hợp lệ
* DONE → IN\_PROGRESS không hợp lệ
* CANCELLED → TODO không hợp lệ
* User không phải assignee không start được task

# Checklist chia nhỏ theo ngày làm

## Ngày 1 — Setup nền

* Tạo Spring Boot project
* Cấu hình MySQL
* Cấu hình Swagger
* Tạo package structure
* Tạo common response
* Tạo global exception handler
* Tạo enum

Kết quả: project chạy được, Swagger mở được.

## Ngày 2 — Entity + Repository

* Tạo User
* Tạo RefreshToken
* Tạo Workspace
* Tạo WorkspaceMember
* Tạo Project
* Tạo ProjectMember
* Tạo Task
* Tạo repository cho từng entity

Kết quả: Hibernate tạo được DB hoặc map đúng DB.

## Ngày 3 — Auth

* Register
* Login
* JWT
* Refresh token
* Logout
* SecurityConfig
* JWT filter

Kết quả: login lấy token và gọi API protected được.

## Ngày 4 — User + Permission foundation

* API profile
* CurrentUserService
* PermissionService
* Check quyền workspace/project/task

Kết quả: có nền phân quyền dùng lại cho các module sau.

## Ngày 5 — Workspace

* Create workspace
* List workspace
* Detail workspace
* Update workspace
* Soft delete workspace
* List members
* Add member by email
* Change member role
* Remove member

Kết quả: frontend làm được Workspace screen.

## Ngày 6 — Project

* Create project
* List project by workspace
* Detail project
* Update project
* Soft delete project
* List project members
* Add project member
* Change project role
* Remove project member

Kết quả: frontend làm được Project screen.

## Ngày 7 — Task CRUD

* Create task
* List task by project
* Filter/search/page/sort
* Detail task
* Update task
* Validate assignee
* Validate due date
* Chặn update task final state

Kết quả: frontend render được Task Board và Task Detail.

## Ngày 8 — Task workflow

* Start task
* Submit review
* Approve task
* Request changes
* Cancel task
* Validate transition
* Validate actor

Kết quả: frontend dùng được toàn bộ workflow task.

## Ngày 9 — Polish API cho frontend

* Chuẩn hóa DTO response
* Bổ sung myRole trong workspace/project response
* Bổ sung assignee info trong task response
* CORS cho frontend
* Swagger mô tả đầy đủ
* Test bằng Postman

Kết quả: backend đủ để frontend tích hợp.

## Ngày 10 — Test + fix bug

* Viết service test cho auth/workspace/project/task/workflow
* Test full flow end-to-end
* Fix lỗi permission
* Fix lỗi lazy loading / JSON recursion
* Fix response format

Kết quả: backend v1 ổn định, có API cho frontend.

# Thứ tự ưu tiên nếu muốn làm nhanh nhất

Nếu muốn có API cho frontend càng sớm càng tốt, làm theo thứ tự rút gọn này:

1. Setup project

2. Entity + DB

3. Auth + JWT

4. Workspace CRUD

5. Workspace Member

6. Project CRUD

7. Project Member

8. Task CRUD

9. Task Status Workflow

10. Swagger + Postman

Chưa cần làm ngay:

Comment

Attachment

Activity Log

Dashboard

Notification

Drag-drop Kanban

Task soft delete

Email invitation thật

Vì các phần đó thuộc v2/v3 trong tài liệu, không phải điều kiện bắt buộc để frontend bắt đầu tích hợp core flow.