====================================================
  TASKFORGE — Team Task Manager (Full-Stack)
  Built with: Java 17 + Spring Boot 3.2 + PostgreSQL
====================================================

TECH STACK
----------
Backend  : Java 17, Spring Boot 3.2
Security : Spring Security + JWT (stateless)
Database : PostgreSQL (prod) / H2 in-memory (local dev)
ORM      : Spring Data JPA + Hibernate
Frontend : Vanilla JS + HTML/CSS (SPA, no build step)
Deploy   : Railway

----------------------------------------------------
FEATURES
----------------------------------------------------
[Auth]
  - JWT-based authentication (stateless)
  - Signup / Login
  - First registered user auto-becomes ADMIN
  - Role-based access: ADMIN, MEMBER

[Projects]
  - Create, read, update, delete projects
  - Add/remove team members
  - Track progress via task completion %
  - Project statuses: ACTIVE, COMPLETED, ARCHIVED
  - Deadline support

[Tasks]
  - Create tasks inside projects
  - Assign to any user
  - Status tracking: TODO → IN_PROGRESS → REVIEW → DONE
  - Priority levels: LOW, MEDIUM, HIGH, CRITICAL
  - Due date + overdue detection
  - Kanban board view per project

[Dashboard]
  - Stats: total projects, tasks, completed, overdue
  - Recent projects with progress bars
  - My assigned tasks
  - Overdue task alerts

[Admin Panel]
  - View all users
  - Promote users to Admin role

----------------------------------------------------
REST API ENDPOINTS
----------------------------------------------------
POST   /api/auth/signup              Register
POST   /api/auth/login               Login (returns JWT)
GET    /api/auth/me                  Current user info

GET    /api/projects                 List my projects
POST   /api/projects                 Create project
GET    /api/projects/{id}            Get project details
PUT    /api/projects/{id}            Update project
DELETE /api/projects/{id}            Delete project
POST   /api/projects/{id}/members/{uid}   Add member
DELETE /api/projects/{id}/members/{uid}   Remove member

GET    /api/projects/{id}/tasks      Get tasks for project
POST   /api/projects/{id}/tasks      Create task in project
GET    /api/tasks/{id}               Get task details
PUT    /api/tasks/{id}               Update task
DELETE /api/tasks/{id}               Delete task
GET    /api/tasks/my                 Get my assigned tasks

GET    /api/dashboard                Dashboard stats + data
GET    /api/users                    List all users
GET    /api/users/me                 My profile

GET    /api/admin/users              Admin: all users
PUT    /api/admin/users/{id}/promote Admin: promote to admin

----------------------------------------------------
RUNNING LOCALLY
----------------------------------------------------
Requirements: Java 17+, Maven 3.x

  mvn spring-boot:run

App runs at: http://localhost:8080
H2 Console : http://localhost:8080/h2-console
  JDBC URL : jdbc:h2:mem:taskmanagerdb

----------------------------------------------------
RAILWAY DEPLOYMENT
----------------------------------------------------
1. Push code to GitHub
2. On Railway: New Project → Deploy from GitHub repo
3. Add PostgreSQL plugin
4. Set environment variables:
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=jdbc:postgresql://<host>:<port>/<db>
   PGUSER=<username>
   PGPASSWORD=<password>
   JWT_SECRET=<your-secure-random-secret>
5. Railway auto-detects Maven, builds + deploys

----------------------------------------------------
PROJECT STRUCTURE
----------------------------------------------------
src/main/java/com/taskmanager/
  ├── model/           Entity classes (User, Project, Task)
  ├── repository/      JPA repositories
  ├── service/         Business logic
  ├── controller/      REST API endpoints
  ├── dto/             Request/Response DTOs
  ├── security/        JWT + Spring Security config
  └── config/          Security & Web config

src/main/resources/
  ├── static/index.html    Frontend SPA
  ├── application.properties
  └── application-prod.properties

----------------------------------------------------
AUTHOR
----------------------------------------------------
Sai Ramesh Latke
Final Year B.E. Computer Engineering
MGM's College of Engineering and Technology, Navi Mumbai
