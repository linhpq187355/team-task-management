# Demo Seed Data Summary

File SQL tuong ung: `02-seed-demo-data.sql`

Script nay tao data mau co ID co dinh de demo va test backend qua Swagger. Tat ca tai khoan demo dung chung mat khau:

```text
password
```

Hash trong SQL duoc tao bang `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`.

## Accounts

| ID | Email | Vai tro chinh | Ghi chu |
| --- | --- | --- | --- |
| 1001 | `owner.demo@g5.local` | Workspace Owner | Co quyen cao nhat trong workspace `2001`, quan ly moi project/task trong workspace. |
| 1002 | `pm.demo@g5.local` | Project Manager | La PM cua project `3001`, owner cua workspace rieng `2002`. |
| 1003 | `dev1.demo@g5.local` | Developer | Developer trong project `3001`, assignee cua cac task workflow chinh. |
| 1004 | `dev2.demo@g5.local` | Developer / PM | Developer trong `3001`, PM cua project `3002`. |
| 1005 | `member.no.project@g5.local` | Workspace Member | Thuoc workspace `2001` nhung khong thuoc project nao. Dung test gioi han quyen project. |
| 1006 | `outsider.demo@g5.local` | Outsider | Khong thuoc workspace demo. Dung test forbidden/access denied. |
| 1007 | `disabled.demo@g5.local` | Disabled user | User trang thai `DISABLED`, dung neu can test user status. |

## Workspaces

| ID | Ten | Trang thai | Ghi chu |
| --- | --- | --- | --- |
| 2001 | G5 Demo Workspace | Active | Workspace chinh de demo owner/member/project/task. |
| 2002 | PM Private Workspace | Active | Workspace rieng cua `pm.demo@g5.local`, dung test cach ly workspace. |
| 2003 | Deleted Workspace | Soft-deleted | Co `deleted_at`, khong nen hien trong API list binh thuong. |

## Projects

| ID | Workspace | Ten | Trang thai | Members |
| --- | --- | --- | --- | --- |
| 3001 | 2001 | Backend Core API | Active | PM: `1002`; Developers: `1003`, `1004`. |
| 3002 | 2001 | Frontend Integration | Active | PM: `1004`; Developer: `1003`. |
| 3003 | 2001 | Archived Mobile Prototype | Soft-deleted | Dung test project bi an khoi API list/detail. |
| 3004 | 2002 | PM Workspace Project | Active | PM: `1002`; Developer: `1003`. |

## Main Tasks

| ID | Project | Status | Priority | Assignee | Case demo |
| --- | --- | --- | --- | --- | --- |
| 4001 | 3001 | TODO | HIGH | 1003 | Login `dev1`, goi `PATCH /api/tasks/4001/start`. |
| 4002 | 3001 | IN_PROGRESS | MEDIUM | 1003 | Login `dev1`, goi `PATCH /api/tasks/4002/submit-review`. |
| 4003 | 3001 | REVIEW | URGENT | 1003 | Login Owner/PM, goi approve, request-changes, hoac cancel. |
| 4004 | 3001 | TODO | LOW | null | Goi start se fail vi chua co assignee. |
| 4005 | 3001 | DONE | HIGH | 1004 | Test khong update/chuyen trang thai final-state. |
| 4006 | 3001 | CANCELLED | MEDIUM | 1004 | Test khong update/chuyen trang thai final-state. |
| 4007 | 3001 | REVIEW | MEDIUM | 1004 | Test assignee-specific restriction voi `dev2`. |
| 4008 | 3001 | TODO | URGENT | 1004 | Test filter/search: `keyword=login`, `priority=URGENT`, `assigneeId=1004`. |
| 4009 | 3001 | TODO | LOW | 1003 | Soft-deleted task, co `deleted_at`, khong nen hien trong API. |
| 4010 | 3002 | TODO | HIGH | 1003 | Task o project khac trong workspace `2001`. |
| 4011 | 3004 | TODO | MEDIUM | 1003 | Task o workspace `2002`, dung test workspace isolation. |

## Suggested Swagger Demo Flow

1. Login `owner.demo@g5.local` / `password`, copy access token vao Swagger Authorize.
2. Goi `GET /api/workspaces` de thay workspace `2001` va khong thay workspace soft-deleted `2003`.
3. Goi `GET /api/workspaces/2001/projects`; owner thay duoc project active `3001`, `3002`.
4. Login `pm.demo@g5.local`, goi `GET /api/projects/3001`, sau do tao/update task trong project `3001`.
5. Login `dev1.demo@g5.local`, goi `PATCH /api/tasks/4001/start`, roi `PATCH /api/tasks/4002/submit-review`.
6. Login `owner.demo@g5.local` hoac `pm.demo@g5.local`, goi `PATCH /api/tasks/4003/approve`.
7. Test negative cases:
   - `PATCH /api/tasks/4004/start` fail vi task chua co assignee.
   - `PUT /api/tasks/4005` fail vi task da DONE.
   - Login `member.no.project@g5.local` roi goi `GET /api/projects/3001` de test forbidden.
   - Login `outsider.demo@g5.local` roi goi `GET /api/workspaces/2001` de test forbidden.

## Useful Query Examples

```text
GET /api/projects/3001/tasks?status=TODO
GET /api/projects/3001/tasks?priority=URGENT
GET /api/projects/3001/tasks?assigneeId=1004
GET /api/projects/3001/tasks?keyword=login
GET /api/projects/3001/tasks?page=0&size=5&sort=dueDate,asc
```

## Notes

- Script co the chay lai nhieu lan: no xoa rieng cac ban ghi demo trong dai ID co dinh roi insert lai.
- Docker MySQL chi tu dong chay file init khi volume database moi duoc tao. Neu DB da ton tai, hay chay thu cong `02-seed-demo-data.sql`.
- Data V2/V3 mau gom comment, attachment, activity log, invitation, notification de co san neu can kiem tra truc tiep DB.
