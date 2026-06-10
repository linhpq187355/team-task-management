# AI Agent Context: Team Task Management System (Backend)

Tài liệu này cung cấp toàn bộ bối cảnh nghiệp vụ, kiến trúc hệ thống, quy tắc ràng buộc (Business Rules) và lộ trình phát triển của dự án "Team Task Management System". AI Agent cần đọc và tuân thủ nghiêm ngặt các chỉ dẫn này trước khi tiến hành sinh mã nguồn (code generation).

---

## 1. TECH STACK & KIẾN TRÚC PHẦN MỀM

* **Ngôn ngữ & Framework:** Java 17, Spring Boot 3.x
* **Quản lý Thư viện:** Maven
* **Bảo mật & Xác thực:** Spring Security + JWT (Access Token ngắn hạn & Refresh Token lưu DB)
* **Tương tác Cơ sở Dữ liệu:** Spring Data JPA / Hibernate
* **Cơ sở Dữ liệu:** MySQL 8.x
* **Kiến trúc Code:** Layered Architecture (Controller -> Service -> Repository -> Entity)
* **Dữ liệu truyền tải:** Sử dụng DTOs (Data Transfer Objects) cho Request/Response. Không lộ trực tiếp Entity ra REST API.

---

## 2. PHẠM VI PHÁT TRIỂN: VERSION 1 — CORE SYSTEM (MVP)

AI chỉ tập trung xây dựng các bảng và tính năng thuộc **Version 1**. KHÔNG tự ý thêm các tính năng/bảng thuộc v2/v3 (Comment, Attachment, Notification, Kanban Drag-drop) trừ khi có yêu cầu cụ thể.

### Danh sách các bảng DB thuộc V1:
1.  `users`: Thông tin tài khoản người dùng.
2.  `refresh_tokens`: Lưu Refresh Token phục vụ cơ chế JWT.
3.  `workspaces`: Không gian làm việc (Có hỗ trợ soft delete: `deleted_at`).
4.  `workspace_members`: Quan hệ Many-to-Many giữa User và Workspace + Role (`OWNER` / `MEMBER`).
5.  `projects`: Dự án thuộc Workspace (Có hỗ trợ soft delete: `deleted_at`).
6.  `project_members`: Quan hệ Many-to-Many giữa User và Project + Role (`PROJECT_MANAGER` / `DEVELOPER`).
7.  `work_items`: Lưu thông tin công việc (**LƯU Ý:** Tên bảng bắt buộc là `work_items` để dự phòng mở rộng cho BUG/ISSUE ở v2. Trong V1, trường `type` (ENUM) mặc định và luôn luôn có giá trị là `TASK`. Bản ghi thuộc V1 KHÔNG có cột `deleted_at`).

---

## 3. HỆ THỐNG ROLE & PHÂN QUYỀN (IMPORTANT)

Hệ thống áp dụng mô hình phân quyền 2 cấp độc lập nhưng có tính kế thừa:

### Cấp 1: Workspace Role
* **OWNER:** Người tạo workspace. Có toàn quyền trên mọi project và member trong workspace đó.
* **MEMBER:** Thành viên thường. Chỉ được thấy và tham gia các project khi được add vào `project_members`.
* *QUY TẮC TỐI CAO:* **Workspace OWNER mặc định có quyền của Project Manager trên mọi Project thuộc workspace**, kể cả khi không có tên trong bảng `project_members`. Luôn luôn bypass qua các bước check quyền PM nếu user là Workspace Owner.

### Cấp 2: Project Role
* **PROJECT_MANAGER (PM):** Quản lý dự án, tạo/giao work_item, duyệt, hủy work_item.
* **DEVELOPER:** Nhận công việc, thực hiện, submit review. Chỉ có thể được assign vào work_item.

### Định nghĩa các Actor trong Code:
* `Guest`: Chưa đăng nhập.
* `User`: Đã đăng nhập, chưa xét role ngữ cảnh.
* `Workspace Owner`: Chủ sở hữu Workspace.
* `Project Manager`: Quản lý Project.
* `Developer`: Thành viên dự án.
* `Assignee`: Developer cụ thể được giao một Work Item (`assignee_id == user.id`).

---

## 4. QUY TRÌNH CHUYỂN TRẠNG THÁI WORK ITEM (WORKFLOW)

Trạng thái của `work_items` (với `type = 'TASK'`) di chuyển nghiêm ngặt theo luồng sau. Bất kỳ sự chuyển đổi trạng thái nào nằm ngoài bảng này đều là **HỢP LỆ = FALSE** và phải ném ra `BadRequestException`.

| Trạng thái hiện tại | Trạng thái đích | Hành động | Actor hợp lệ | Ràng buộc nghiệp vụ |
| :--- | :--- | :--- | :--- | :--- |
| **📋 TODO** | ⚡ IN_PROGRESS | Start Task | **Assignee** | Chỉ Assignee được bấm. Work Item bắt buộc phải có `assignee_id`. |
| **📋 TODO** | ❌ CANCELLED | Cancel Task | **PM / Workspace Owner** | Hủy công việc khi không còn cần thiết. |
| **⚡ IN_PROGRESS** | 🔍 REVIEW | Submit for Review | **Assignee** | Developer nộp bài để review. |
| **⚡ IN_PROGRESS** | ❌ CANCELLED | Cancel Task | **PM / Workspace Owner** | Hủy giữa chừng. |
| **🔍 REVIEW** | ✅ DONE | Approve / Done | **PM / Workspace Owner** | PM duyệt chất lượng đạt yêu cầu. |
| **🔍 REVIEW** | ⚡ IN_PROGRESS | Request Changes | **PM / Workspace Owner** | PM từ chối, yêu cầu sửa lại (yêu cầu thay đổi). |
| **🔍 REVIEW** | ❌ CANCELLED | Cancel Task | **PM / Workspace Owner** | Hủy sau khi review. |
| **✅ DONE** | *Bất kỳ trạng thái nào* | KHÔNG CHO PHÉP | Không ai có quyền | **Final State.** Trạng thái đóng, không đảo ngược. |
| **❌ CANCELLED** | *Bất kỳ trạng thái nào* | KHÔNG CHO PHÉP | Không ai có quyền | **Final State.** Trạng thái đóng, không đảo ngược. |

---

## 5. CÁC QUY TẮC NGHIỆP VỤ BẮT BUỘC (BUSINESS RULES)

### Quy tắc về Work Item (Type = 'TASK'):
1.  Chỉ **Project Manager** hoặc **Workspace Owner** mới có quyền tạo Work Item.
2.  `title` không được rỗng.
3.  `type` phải được set giá trị là `TASK` (sử dụng ENUM gồm `TASK` và `BUG`).
4.  `due_date` phải `>= ngày tạo` (So sánh phần ngày `DATE` với `DATE`, không so sánh giờ `DATETIME`). Khi update `due_date`, giá trị mới cũng không được nhỏ hơn ngày tạo ban đầu.
5.  Mỗi work item tại một thời điểm chỉ có tối đa **1 assignee**.
6.  `Assignee` phải là thành viên trong dự án (`project_members`) và có role là `DEVELOPER`.
7.  Work item có thể tạo mà không cần gán người ngay (`assignee_id = null`). Tuy nhiên, khi chuyển từ `TODO` sang `IN_PROGRESS`, hệ thống phải validate kiểm tra xem đã điền `assignee_id` chưa.
8.  Nếu Work item đã ở trạng thái final (`DONE` hoặc `CANCELLED`), hệ thống **KHÔNG CHO PHÉP** chỉnh sửa các thông tin: `title`, `description`, `priority`, `due_date`, `assignee_id`.

### Quy tắc về Workspace & Project:
1.  Người tạo Workspace tự động trở thành `OWNER`.
2.  Không cho phép xóa `OWNER` duy nhất ra khỏi Workspace.
3.  Khi một thành viên bị xóa khỏi Workspace, họ lập tức mất quyền truy cập vào tất cả các Project và Work Items thuộc Workspace đó.
4.  Mỗi Project bắt buộc phải thuộc về một Workspace cụ thể. Người tạo Project tự động có role `PROJECT_MANAGER`.
5.  Project có thuộc tính thời gian: `start_date` không được sau `end_date`.

### Quy tắc Xóa mềm (Soft Delete & Cascade):
1.  Khi Workspace bị xóa mềm (`deleted_at != null`), tất cả các Project và Work Items nằm bên trong sẽ tự động không được hiển thị ở các API lấy danh sách thông thường.
2.  Khi Project bị xóa mềm, tất cả Work Items bên trong sẽ ẩn đi.
3.  *Lưu ý cho V1:* Chỉ có Workspace và Project áp dụng Soft Delete (`deleted_at`). Đối với `work_items`, V1 không dùng soft delete kỹ thuật mà dùng trạng thái nghiệp vụ `CANCELLED`.

### Quy tắc Thành viên Workspace (V1):
1.  Tính năng mời thành viên ở V1 hoạt động theo cơ chế: Thêm trực tiếp bằng Email của User **đã tồn tại sẵn** trong hệ thống. Hệ thống phải kiểm tra xem email có tồn tại trong bảng `users` hay chưa trước khi tạo bản ghi trong `workspace_members`.

---

## 6. CHỈ DẪN GENERATE CODE CHO AI

* **Validation:** Luôn sử dụng `@Valid` và các annotation như `@NotBlank`, `@NotNull`, `@Min` trong DTOs để validate dữ liệu đầu vào tại Controller.
* **Exception Handling:** Xây dựng một `@ControllerAdvice` tập trung để xử lý các ngoại lệ (`ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException`). Trả về cấu hình JSON lỗi đồng nhất: `{ "timestamp", "status", "error", "message", "path" }`.
* **Kiểm tra quyền (Authorization):** Việc check quyền giữa Workspace Owner, Project Manager và Developer cần được xử lý ở tầng Service hoặc custom AOP/Spring Security Expression một cách tường minh, đảm bảo tuân thủ đúng quy tắc kế thừa quyền của `Workspace Owner`.
* **Format ngày tháng:** Định dạng JSON cho các trường ngày tháng là `yyyy-MM-dd` hoặc `yyyy-MM-dd HH:mm:ss`.

Hãy dựa vào toàn bộ quy tắc trên để thiết kế các Entity lớp dữ liệu, Repositories, Services và Controllers chuẩn mực nhất cho dự án V1.