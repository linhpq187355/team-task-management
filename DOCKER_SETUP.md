# Hướng Dẫn Setup Docker cho Team Task Management

## 📋 Yêu Cầu Hệ Thống
- **Docker**: Phiên bản 20.10+ ([Tải Docker](https://www.docker.com/products/docker-desktop))
- **Docker Compose**: Phiên bản 2.0+ (thường đi kèm với Docker Desktop)
- **Git**: Để clone project (nếu cần)

## 🚀 Các Bước Setup

### 1. **Kiểm Tra Docker và Docker Compose**
```bash
docker --version
docker compose version
```

### 2. **File Cấu Hình**
File `.env` đã được tạo với các biến môi trường mặc định:
```
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=team_task_management
MYSQL_ROOT_PASSWORD=root123456
MYSQL_USER=teamtask
MYSQL_PASSWORD=teamtask123456
```

**⚠️ Lưu ý**: Nên thay đổi mật khẩu trong file `.env` cho môi trường production!

### 3. **Chạy MySQL Database**

#### Cách 1: Chỉ chạy Database (Khuyến Nghị cho Phát Triển)
```bash
cd D:\g5\team-task-management
docker compose up -d mysql db-init
```

**Điều này sẽ**:
- Khởi động MySQL container (port: 3307)
- Chạy initialization script
- Tạo database và user

**Để kiểm tra logs**:
```bash
docker compose logs -f mysql
docker compose logs -f db-init
```

**Để dừng**:
```bash
docker compose down
```

### 4. **Chạy Ứng Dụng Spring Boot (Cách A: Local)**

Sau khi MySQL đang chạy, chạy app trên máy local:

```bash
cd D:\g5\team-task-management
./mvnw clean package
java -jar target/team-task-management-0.0.1-SNAPSHOT.jar
```

Hoặc với Maven trực tiếp:
```bash
./mvnw spring-boot:run
```

### 5. **Chạy Ứng Dụng trong Docker (Cách B: Full Docker)**

Để chạy cả database và application trong Docker:

```bash
cd D:\g5\team-task-management

# Build image ứng dụng
docker build -t team-task-management:latest .

# Cập nhật docker-compose.yml để thêm app service (nếu cần)
# Sau đó chạy:
docker compose up -d
```

## 📊 Truy Cập Ứng Dụng

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **MySQL**: 
  - Host: `localhost` 
  - Port: `3307`
  - User: `teamtask`
  - Password: `teamtask123456`

## 🔧 Các Lệnh Docker Hữu Ích

### Xem trạng thái container
```bash
docker compose ps
```

### Xem logs
```bash
docker compose logs -f                    # Tất cả logs
docker compose logs -f mysql              # Chỉ MySQL
docker compose logs -f db-init            # Chỉ init script
```

### Dừng tất cả container
```bash
docker compose down
```

### Xóa toàn bộ volume (⚠️ Xóa database)
```bash
docker compose down -v
```

### Restart container
```bash
docker compose restart mysql
```

### Kết nối trực tiếp vào MySQL container
```bash
docker compose exec mysql mysql -u teamtask -p team_task_management
# Password: teamtask123456
```

## 📝 Database Initialization

File init script: `docker/mysql/init/01-reset-database.sql`

Script này sẽ tự động:
1. Tạo database `team_task_management`
2. Tạo user `teamtask` với password từ `.env`
3. Gán quyền cho user

## 🐛 Khắc Phục Sự Cố

### MySQL không khởi động
```bash
docker compose down -v
docker compose up -d mysql db-init
```

### Port 3307 đã được sử dụng
Thay đổi port trong `docker-compose.yml`:
```yaml
ports:
  - "3308:3306"  # Thay 3307 thành 3308
```

### Kiểm tra file .env
```bash
cat .env  # Linux/Mac
type .env  # Windows
```

## 🎯 Quy Trình Phát Triển Được Khuyến Nghị

1. **Chạy Database**:
   ```bash
   docker compose up -d mysql db-init
   ```

2. **Chạy Application Locally**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Phát triển code** và test

4. **Dừng khi xong**:
   ```bash
   docker compose down
   ```

Cách này giúp bạn:
- Phát triển nhanh hơn (không cần rebuild Docker image)
- Dễ debug hơn
- Linh hoạt hơn trong quá trình phát triển

## 📚 Tài Liệu Thêm

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
