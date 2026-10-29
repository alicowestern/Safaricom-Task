# Task Manager — Safaricom Ethiopia Technical Assessment

A full-stack task management application with a Spring Boot REST API backend and a React (Vite) frontend.

## Prerequisites

- **Java 17** (JDK)
- **Maven 3.9+** (or use the included Maven wrapper `./mvnw`)
- **Node.js 18+** and npm
- **Docker** (optional, for containerized backend)

## Technology Stack

| Layer     | Technology                                |
|-----------|-------------------------------------------|
| Backend   | Java 17, Spring Boot 3.2.5, Maven        |
| Frontend  | React 18, Vite 5, JavaScript, CSS        |
| Storage   | In-memory ConcurrentHashMap               |
| Validation| Jakarta Bean Validation                   |
| Container | Docker (multi-stage build)                |

## Project Structure

```
safaricom-task-manager/
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/java/com/safaricom/taskmanager/
│   │   ├── TaskManagerApplication.java
│   │   ├── config/CorsConfig.java
│   │   ├── controller/TaskController.java
│   │   ├── dto/
│   │   │   ├── ErrorResponse.java
│   │   │   ├── TaskRequest.java
│   │   │   └── TaskResponse.java
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── TaskNotFoundException.java
│   │   ├── model/
│   │   │   ├── Task.java
│   │   │   └── TaskStatus.java
│   │   ├── repository/TaskRepository.java
│   │   └── service/TaskService.java
│   └── src/test/java/.../TaskManagerApplicationTests.java
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       └── App.css
├── .gitignore
└── README.md
```

## Running the Application

### Backend

```bash
cd backend
mvn spring-boot:run
```

The API starts at **http://localhost:8081**.

Alternatively, build and run the JAR:

```bash
cd backend
mvn package -DskipTests
java -jar target/task-manager-1.0.0.jar
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts at **http://localhost:5173**.

### Docker (Backend Only)

```bash
cd backend
docker build -t safaricom-task-api .
docker run --rm -p 8081:8081 safaricom-task-api
```

The containerized API is accessible at **http://localhost:8081/api/tasks**.

## API Endpoints

| Method | Endpoint          | Description          | Success | Error       |
|--------|-------------------|----------------------|---------|-------------|
| GET    | /api/tasks        | List all tasks       | 200     | —           |
| GET    | /api/tasks/{id}   | Get task by ID       | 200     | 404         |
| POST   | /api/tasks        | Create a new task    | 201     | 400         |
| PUT    | /api/tasks/{id}   | Update a task (full) | 200     | 400, 404    |
| DELETE | /api/tasks/{id}   | Delete a task        | 204     | 404         |

### Example Request

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Prepare report", "description": "Draft weekly report", "status": "TODO"}'
```

### Task Model

| Field       | Type   | Constraints                  |
|-------------|--------|------------------------------|
| id          | Long   | Auto-generated               |
| title       | String | Required, 3–100 characters   |
| description | String | Optional, max 500 characters |
| status      | Enum   | TODO, IN_PROGRESS, DONE      |

- Status defaults to `TODO` if omitted on creation.
- All error responses return JSON with `status` and `message` fields.

## Architecture

```
┌─────────────┐        HTTP/JSON        ┌──────────────────────┐
│  React App  │ ◄────────────────────── │  Spring Boot API     │
│  (Vite)     │        Fetch API        │                      │
│  :5173      │ ───────────────────────►│  :8081               │
└─────────────┘                         │                      │
                                        │  Controller Layer    │
                                        │    ↕ @Valid DTOs     │
                                        │  Service Layer       │
                                        │    ↕ Model mapping   │
                                        │  Repository Layer    │
                                        │    ↕ ConcurrentMap   │
                                        └──────────────────────┘
```

**Backend** follows a three-layer architecture:

1. **Controller** (`TaskController`): Handles HTTP routing, delegates to the service, returns correct status codes.
2. **Service** (`TaskService`): Contains business logic, DTO ↔ model conversion, default value assignment.
3. **Repository** (`TaskRepository`): Thread-safe in-memory storage using `ConcurrentHashMap` with `AtomicLong` ID generation.

**Cross-cutting concerns:**
- `GlobalExceptionHandler` provides consistent JSON error responses for validation, not-found, malformed JSON, and unexpected errors.
- `CorsConfig` enables the React dev server to call the API.
- Jakarta Bean Validation annotations on `TaskRequest` DTO enforce title/description constraints.

**Frontend** is a single-page React application that:
- Fetches tasks from the backend on load.
- Provides create/edit/delete operations via the Fetch API.
- Validates input client-side before submission.
- Shows loading indicators, error banners, and success messages.
- Requires explicit user confirmation before deletion.
- Resets/cancels editing with a dedicated button.

## Running Tests

```bash
cd backend
mvn test
```

Runs 17 integration tests covering all endpoint scenarios including validation edge cases.
