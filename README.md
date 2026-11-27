# Smart City Issue Reporting Platform

Full-stack web app (Java 17 + Spring Boot 3, Spring MVC REST APIs, Spring Security, MySQL, HTML/CSS/JavaScript, Leaflet + OpenStreetMap).

## Prerequisites
* JDK 17 or newer
* Maven 3.8+
* MySQL 8 (or use the built-in H2 quick-start profile)

## Run with MySQL
1. Make sure MySQL is running. The database `smartcity_db` is created automatically.
2. Set your credentials (defaults: root / root):
   * Windows PowerShell: `$env:DB_USERNAME="root"; $env:DB_PASSWORD="yourpassword"`
   * Linux/macOS: `export DB_USERNAME=root DB_PASSWORD=yourpassword`
3. `mvn spring-boot:run`
4. Open http://localhost:8080

## Quick start without MySQL (H2 file database)
`mvn spring-boot:run -Dspring-boot.run.profiles=h2`

## Demo accounts (created automatically on first start)
| Role    | Email                  | Password    |
|---------|------------------------|-------------|
| Admin   | admin@smartcity.gov    | Admin@123   |
| Officer | officer@smartcity.gov  | Officer@123 |
| Citizen | register from the home page | – |

Change these passwords (see `DataSeeder.java`) before any real deployment.

## Project layout
```
src/main/java/com/smartcity/issuereporting
 ├─ config/       SecurityConfig, WebConfig, GlobalExceptionHandler, DataSeeder
 ├─ model/        JPA entities + enums (User, Complaint, Department, ComplaintUpdate, Notification)
 ├─ repository/   Spring Data JPA repositories
 ├─ dto/          request / response records
 ├─ service/      business logic (ComplaintService, UserService, NotificationService, FileStorageService)
 └─ controller/   REST controllers (Auth, Complaint, User, Admin, Officer)
src/main/resources/static   HTML pages, css/style.css, js/app.js
uploads/                    complaint photographs (local server storage)
```

## REST API summary
| Method & path | Role | Purpose |
|---|---|---|
| POST /api/auth/register | public | citizen registration |
| POST /api/auth/login, /logout; GET /api/auth/me | public/any | session auth |
| POST /api/complaints (multipart) | CITIZEN | submit complaint with photo, category, location |
| GET /api/complaints/mine | CITIZEN | own complaints |
| GET /api/complaints/{id-code} | owner / assigned officer / admin | track + history |
| GET /api/notifications, POST /api/notifications/read-all | any | notifications |
| GET/PUT /api/profile, PUT /api/profile/password | any | profile |
| GET /api/admin/complaints?category&status&priority&q | ADMIN | filter complaints |
| PUT /api/admin/complaints/{id}/assign, /status | ADMIN | assign dept/officer/priority, update status |
| GET /api/admin/stats, /report.csv | ADMIN | analytics, CSV report |
| GET /api/admin/users, PUT /api/admin/users/{id}/enabled | ADMIN | user management |
| POST /api/admin/officers, /departments | ADMIN | create officers / departments |
| GET /api/officer/complaints, PUT /api/officer/complaints/{id}/status | OFFICER | assigned work |

## Notes
* Maps need internet access (OpenStreetMap tiles, Leaflet and Chart.js are loaded from CDNs).
* CSRF protection is disabled for simplicity (same-origin session app). Enable it for production.
* Uploaded images are validated by content type and stored under random names in `uploads/`.


// for running the project
1->  terminal code
cd C:\Users\aryan\Downloads\smart-city-issue-reporting-project\smart-city-issue-reporting

2->  terminal code
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory | Where-Object Name -like "jdk-21*" | Select-Object -First 1).FullName
$env:Path = "$env:JAVA_HOME\bin;C:\maven\apache-maven-3.10.2\bin;$env:Path"

3->  terminal code
$env:PORT = "8081"
mvn spring-boot:run "-Dspring-boot.run.profiles=h2"

4-> run this link on any browser
http://localhost:8081.