# CodeCraftHub

A simple personalized learning platform API where developers can track the
courses they want to learn. Built with **Java + Spring Boot**, storing data
in a plain **JSON file** (`courses.json`) — no database, no authentication,
just REST API basics.

## Table of contents

1. [Project overview and features](#1-project-overview-and-features)
2. [Installation instructions](#2-installation-instructions)
3. [How to run the application](#3-how-to-run-the-application)
4. [API endpoint documentation with examples](#4-api-endpoint-documentation-with-examples)
5. [Troubleshooting guide](#5-troubleshooting-guide)

## 1. Project overview and features

CodeCraftHub is a learning project that demonstrates REST API basics with
Spring Boot. There's no database and no login — just a single JSON file
(`courses.json`) that stores the list of courses you want to track.

**Features**

- Full CRUD (Create, Read, Update, Delete) REST API for courses.
- Each course tracks: `name`, `description`, `target_date`, `status`, plus
  auto-generated `id` and `created_at` fields.
- `status` is restricted to exactly `"Not Started"`, `"In Progress"`, or
  `"Completed"`.
- Data persists in `courses.json`, which is created automatically the first
  time the app starts — no manual setup required.
- Clean JSON error responses for missing fields, invalid status values,
  malformed requests, and "not found" courses.
- No authentication, no external services — easy to read, run, and extend.

**Project structure**

```
ProyectoSample/
├── pom.xml                                  Maven build file (dependencies, plugin)
├── courses.json                             The "database" — auto-created on first run
└── src/
    ├── main/
    │   ├── java/com/codecrafthub/
    │   │   ├── CodeCraftHubApplication.java  Spring Boot entry point (main method)
    │   │   ├── model/
    │   │   │   └── Course.java               The Course data model + validation rules
    │   │   ├── service/
    │   │   │   └── CourseService.java        CRUD logic + reads/writes courses.json
    │   │   └── controller/
    │   │       └── CourseController.java     REST endpoints + error handling
    │   └── resources/
    │       └── application.properties        Server port + path to the JSON file
    └── test/                                  (add tests here as you learn)
```

This project uses a simple **Controller → Service** layering (no separate
repository/database layer is needed for a JSON-file-backed app like this):

- **Controller** — exposes HTTP endpoints, reads request data, returns
  responses, and converts exceptions into clean JSON error bodies.
- **Service** — holds business rules (id generation, status validation,
  "not found" checks) **and** all file I/O for `courses.json`.

## 2. Installation instructions

### Prerequisites

| Tool | Minimum version | Check with |
|------|------------------|------------|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ | `mvn -version` |

> Maven isn't strictly required if you use the bundled wrapper (`mvnw` /
> `mvnw.cmd`) — but this project currently assumes a system-installed Maven.

### Get the code

If you already have the project folder (e.g. `ProyectoSample`), just open a
terminal there. Otherwise clone/copy the project, then:

```powershell
cd path\to\ProyectoSample
```

### Install dependencies

Maven downloads all dependencies declared in `pom.xml` (Spring Boot Web
starter, Validation starter, Jackson, etc.) automatically — there's no
separate "install" step like `npm install`. Just build once to fetch
everything and confirm it compiles:

```powershell
mvn clean install
```

This downloads dependencies into your local `~/.m2` repository and produces
a runnable JAR in `target/`.

## 3. How to run the application

### Option A: Run with Maven (recommended while developing)

```powershell
mvn spring-boot:run
```

### Option B: Run the packaged JAR

```powershell
mvn clean package
java -jar target/codecrafthub-0.0.1-SNAPSHOT.jar
```

Either way, once started you'll see Spring Boot's startup log ending with
something like `Started CodeCraftHubApplication in X seconds`, and the API
will be available at:

```
http://localhost:8080
```

The server port and the path to the JSON data file are both configurable in
[application.properties](src/main/resources/application.properties):

```properties
server.port=8080
data.file.path=courses.json
```

On first startup, `courses.json` is created automatically (as an empty `[]`)
if it doesn't already exist — there's nothing to seed or configure manually.

## 4. API endpoint documentation with examples

Base path: `/api/courses`

| Method | Path                | Description           | Body               |
|--------|---------------------|------------------------|---------------------|
| POST   | `/api/courses`      | Add a new course       | Course JSON (no `id`/`created_at`) |
| GET    | `/api/courses`      | Get all courses        | –                   |
| GET    | `/api/courses/{id}` | Get a specific course  | –                   |
| PUT    | `/api/courses/{id}` | Update a course        | Course JSON         |
| DELETE | `/api/courses/{id}` | Delete a course        | –                   |

### Course JSON shape

```json
{
  "id": 1,
  "name": "Spring Boot Fundamentals",
  "description": "Learn the basics of building REST APIs with Spring Boot.",
  "target_date": "2026-11-15",
  "status": "In Progress",
  "created_at": "2026-09-30T10:15:30"
}
```

- `status` must be exactly one of: `"Not Started"`, `"In Progress"`, `"Completed"`.
- `id` is an auto-incrementing integer starting at 1 — generated on create, never sent by the client.
- `created_at` is an auto-generated timestamp — generated on create, never sent by the client.
- `name`, `description`, `target_date`, and `status` are required in requests.

### POST /api/courses — Add a new course

Request:

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Docker Basics\",\"description\":\"Containers 101\",\"target_date\":\"2026-10-31\",\"status\":\"Not Started\"}"
```

Response (`201 Created`):

```json
{
  "id": 1,
  "name": "Docker Basics",
  "description": "Containers 101",
  "target_date": "2026-10-31",
  "status": "Not Started",
  "created_at": "2026-09-30T10:00:00"
}
```

### GET /api/courses — Get all courses

```bash
curl http://localhost:8080/api/courses
```

Response (`200 OK`):

```json
[
  {
    "id": 1,
    "name": "Docker Basics",
    "description": "Containers 101",
    "target_date": "2026-10-31",
    "status": "Not Started",
    "created_at": "2026-09-30T10:00:00"
  }
]
```

### GET /api/courses/{id} — Get a specific course

```bash
curl http://localhost:8080/api/courses/1
```

Response (`200 OK`): a single course object (same shape as above).

### PUT /api/courses/{id} — Update a course

```bash
curl -X PUT http://localhost:8080/api/courses/1 \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Docker Basics\",\"description\":\"Containers 101\",\"target_date\":\"2026-10-31\",\"status\":\"In Progress\"}"
```

Response (`200 OK`): the updated course, with the original `id` and
`created_at` preserved.

### DELETE /api/courses/{id} — Delete a course

```bash
curl -X DELETE http://localhost:8080/api/courses/1
```

Response: `204 No Content` (empty body).

### Error responses

| Situation                          | Status | Example body |
|-------------------------------------|--------|--------------|
| Missing required field              | 400    | `{"name": "name is required"}` |
| Invalid `status` value              | 400    | `{"error": "Invalid status 'Done'. Must be exactly one of: [...]"}` |
| Malformed JSON / bad date format    | 400    | `{"error": "Malformed request body. Check field types and date format (yyyy-MM-dd)."}` |
| Course id not found                 | 404    | `{"error": "Course not found with id: 99"}` |
| File read/write failure             | 500    | `{"error": "Failed to read courses from 'courses.json': ..."}` |

## 5. Troubleshooting guide

**`mvn` is not recognized / command not found**
Maven isn't installed or isn't on your `PATH`. Install Maven and confirm
with `mvn -version`, or use a bundled wrapper (`mvnw.cmd`) if present.

**`JAVA_HOME` not set / wrong Java version errors**
Spring Boot 3.x requires **JDK 21+**. Run `java -version` to confirm. If
multiple JDKs are installed, set `JAVA_HOME` to a JDK 21 install and restart
your terminal.

**Port 8080 already in use (`Web server failed to start`)**
Another process is using port 8080. Either stop that process, or change the
port in `application.properties`:

```properties
server.port=8081
```

**`404 Not Found` for a valid-looking URL**
Make sure the path starts with `/api/courses` (not just `/courses`), and
that `{id}` is a real course id returned from a previous `GET /api/courses`.

**`400 Bad Request` when creating/updating a course**
Check the response body — it tells you exactly what's wrong:
- A field-name key (e.g. `"name"`) means that required field was missing or blank.
- An `"error"` key about `status` means the value wasn't exactly `"Not Started"`, `"In Progress"`, or `"Completed"` (case- and spacing-sensitive).
- An `"error"` key about malformed JSON usually means `target_date` isn't in `yyyy-MM-dd` format, or the JSON body itself has a syntax error (missing quote/comma/brace).

**Changes don't seem to persist / `courses.json` looks unchanged**
Check the working directory the app was started from — `courses.json` is
created relative to that directory (controlled by `data.file.path` in
`application.properties`). If you run the app from a different folder each
time, you may be looking at (or creating) a different file.

**`courses.json` seems corrupted or won't parse**
Since the whole file is rewritten on every change, this is rare, but if it
happens: stop the app, delete `courses.json`, and restart — the app will
recreate it as an empty `[]`. You'll lose existing data, so back up the file
first if you want to keep it.

**500 Internal Server Error on any request**
This usually means a file read/write problem (e.g. permissions issue, disk
full, or the file path points somewhere the app can't access). Check the
`"error"` message in the response body and the application logs in the
terminal where `mvn spring-boot:run` is running for the underlying
`IOException` details.

## Suggested next steps for learning

1. Add a `GET /api/courses?status=In Progress` filter using a `@RequestParam`.
2. Add a `PATCH /api/courses/{id}/status` endpoint to update just the status.
3. Write tests with `@SpringBootTest` + `MockMvc` to exercise each endpoint.
4. Swap `CourseService`'s file I/O for a database (e.g. Spring Data JPA +
   H2/PostgreSQL) once you're comfortable — notice how `CourseController`
   wouldn't need to change.

