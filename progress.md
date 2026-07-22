# Frontend Progress

Frontend nằm ở `frontend/` — Vite + React (JS, không TypeScript), React Router, không dùng UI
library hay state-management ngoài Context API. Gọi API bằng `fetch` thuần (không thêm axios).

## Stack & lý do

- **Vite + React**: CORS backend đã mở sẵn cho `:5173` (Vite mặc định) → khỏi phải chỉnh thêm.
- **react-router-dom**: routing duy nhất cần cài thêm.
- **Context API** cho auth state — không cần Redux/Zustand cho quy mô app này.
- **fetch wrapper tự viết** (`src/api/client.js`) — tự động gắn Bearer token, tự refresh
  access token khi gặp 401 rồi retry 1 lần, hết hạn thì clear session + redirect `/login`.

## Đã xong

- [x] Scaffold Vite React project (`frontend/`), dọn boilerplate mặc định.
- [x] `api/client.js`: fetch wrapper + auto refresh token + xử lý `ApiResponse<T>` wrapper.
- [x] `api/resources.js`: hàm gọi API cho auth/user/workspace/project/task, khớp đúng
      route và DTO trong `controller/*.java` (đọc trực tiếp source, không suy đoán từ spec doc).
- [x] `AuthContext`: login/register/logout/refreshProfile, lưu session vào `localStorage`.
- [x] `ProtectedRoute` + `Layout` (navbar + logout).
- [x] Auth: `LoginPage`, `RegisterPage`.
- [x] `ProfilePage`: xem/sửa profile, đổi mật khẩu.
- [x] `WorkspaceListPage`: danh sách + tạo workspace.
- [x] `WorkspaceDetailPage`: sửa/xoá workspace (Owner), danh sách project, tạo project
      (Owner only theo spec doc), danh sách + thêm/sửa role/xoá workspace member.
- [x] `ProjectDetailPage`: sửa/xoá project (PM/Owner), danh sách + thêm/sửa role/xoá
      project member (chỉ add được user đã có trong workspace, đúng rule).
- [x] `TaskBoardPage`: kanban 5 cột theo đúng state machine, filter theo priority/assignee/
      keyword (client-side, load 1 lần `size=200` thay vì gọi lại API mỗi cột), tạo task
      (PM/Owner only, assignee chỉ chọn được DEVELOPER trong project).
- [x] `TaskDetailPage`: xem chi tiết, các nút hành động Start/Submit review/Approve/
      Request changes/Cancel — chỉ hiện đúng nút theo status hiện tại + role của user
      (assignee vs PM/Owner), sửa task (ẩn nếu task đã DONE/CANCELLED).
- [x] CSS thuần (`index.css`), hỗ trợ light/dark theo `prefers-color-scheme`.
- [x] `npm run build` chạy sạch, không lỗi.

## Cố tình bỏ qua (chưa cần cho V1)

- Comment/attachment/notification/kanban drag-drop — đúng theo scope V1 trong `ai-agents.md`.
- API `addProjectMemberRole` / `removeProjectMemberRole` (gán nhiều role cho 1 member) —
  route đã có ở backend nhưng không nằm trong flow chính của spec doc; UI hiện tại chỉ
  dùng `updateProjectMemberRole` (1 role/member). Thêm sau nếu cần multi-role thật sự.
- Task board đang load tối đa 200 task/project rồi group phía client thay vì phân trang
  thật — đủ dùng cho demo/đồ án, nâng cấp lên infinite-scroll/pagination nếu dữ liệu lớn.
## Đã test end-to-end với backend thật

Docker/MySQL + `spring-boot:run` đã lên, chạy script gọi thẳng REST API (không qua UI)
theo đúng field/route mà `api/resources.js` dùng: register 2 user → login → tạo workspace
(owner) → add member vào workspace → tạo project → add dev làm `DEVELOPER` → tạo task
assign cho dev → dev `start` → dev `submit-review` → owner `approve` → confirm sửa task
`DONE` bị chặn 400 đúng như kỳ vọng. Toàn bộ response field khớp 100% với những gì các
trang React đang đọc (`page.content`, `myRole`/`myRoles`, `userId`/`fullName`/`role` trong
member DTO, v.v.). CORS preflight từ origin `:5173` cũng trả về đúng header cho phép.

Chưa tự bấm qua UI thật trong trình duyệt (môi trường này không có browser-automation
tool) — về logic/API thì đã xác nhận đúng, nhưng nên tự mở `http://localhost:5173` và
click thử luồng 1 lần để bắt các lỗi thuần UI (CSS layout, form UX...).

## Cách chạy

```bash
# Backend (cần trước)
docker compose up -d mysql db-init
./mvnw spring-boot:run

# Frontend
cd frontend
npm install
npm run dev   # http://localhost:5173
```
