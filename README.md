# Config Server – Lưu trữ cấu hình trên Git

Bài tập: https://portal.rikkei.edu.vn/learn/159/homework/8901

Spring Cloud Config Server chạy tại cổng **8888**, đọc cấu hình từ kho Git cục bộ
`medical-config-repo` trên nhánh `main`. Cấu hình nghiệp vụ được tách khỏi mã nguồn
của patient-service, có lịch sử thay đổi bằng Git và được cung cấp tập trung qua HTTP.

## Công nghệ

- Java 21, Spring Boot 3.5.6, Spring Cloud 2025.0.0.
- Gradle Wrapper 8.14.3.
- Dependency `spring-cloud-config-server` và annotation `@EnableConfigServer`.

## Chạy trên macOS / Linux

Cần JDK 21 và Git; Git cần có `user.name` và `user.email` để tạo commit.
Từ thư mục project:

```bash
./scripts/init-config-repo.sh
./gradlew bootRun
```

Script khởi tạo thư mục `../medical-config-repo`, chạy `git init -b main`,
sao chép file mẫu và tạo commit. Script có thể chạy lại và không ghi đè dữ liệu đã có.
Hai thư mục sau nằm cạnh nhau:

```text
IdeaProjects/
├── config-server/
│   ├── config-repo/patient-service.properties  # bản mẫu để khởi tạo
│   ├── scripts/init-config-repo.sh
│   └── src/main/
└── medical-config-repo/                       # kho Git thực tế được server đọc
    ├── .git/
    └── patient-service.properties
```

Chạy Gradle từ thư mục `config-server` để đường dẫn mặc định được phân giải đúng.
Nếu máy có nhiều JDK, chọn JDK 21 bằng `JAVA_HOME` trước khi chạy Gradle.

## Cấu hình

`src/main/resources/application.properties`:

```properties
server.port=8888
spring.application.name=config-server
spring.cloud.config.server.git.uri=${CONFIG_REPO_URI:file://${user.dir}/../medical-config-repo}
spring.cloud.config.server.git.default-label=main
```

Có thể chọn kho khác mà không sửa code:

```bash
CONFIG_REPO_URI=file:///duong/dan/medical-config-repo ./gradlew bootRun
```

Trên Windows, tạo và commit kho bằng Git thủ công:

```powershell
mkdir C:/medical-config-repo
cd C:/medical-config-repo
git init -b main
# Sao chép config-repo/patient-service.properties từ project vào thư mục này.
git add patient-service.properties
git commit -m "Add patient service configuration"
# Quay lại thư mục config-server trước khi chạy:
$env:CONFIG_REPO_URI = 'file:///C:/medical-config-repo'
.\gradlew.bat bootRun
```

Nếu dùng nhánh `master`, đổi `spring.cloud.config.server.git.default-label=master`.

Kho cấu hình chứa đúng dữ liệu mẫu của đề:

```properties
server.port=8081
message=Chao mung den voi Patient Service tu Config Server
db.url=jdbc:mysql://localhost:3306/patient_db
db.username=root
```

`db.url` và `db.username` chỉ là dữ liệu cấu hình mẫu. Bài này không cần chạy MySQL
hay patient-service; Config Server chỉ đọc và trả cấu hình.

## Kiểm tra

```bash
./gradlew clean test bootJar
curl -f http://localhost:8888/patient-service/default
```

Kết quả có `name` là `patient-service`, `profiles` là `["default"]`, `version`
là mã commit Git và `propertySources[0].source` gồm bốn thuộc tính ở trên.
`server.port=8081` trong JSON là cổng của patient-service; Config Server vẫn chạy ở 8888.

Test tích hợp tạo kho Git tạm trên nhánh `main`, commit file mẫu, khởi động server
và kiểm tra HTTP 200, tên service, profile, mã commit và toàn bộ giá trị cấu hình.

Khi thay đổi cấu hình, commit vào kho `medical-config-repo`, rồi gọi lại endpoint.

## Nộp bài

Đưa project này lên GitHub và dán URL repository vào phần nộp bài trên hệ thống.
Bản mẫu và script khởi tạo được lưu cùng project để người chấm có thể tái tạo kho Git cục bộ.

Tham khảo: [Spring Cloud Config Server](https://docs.spring.io/spring-cloud-config/reference/4.3/server.html).
