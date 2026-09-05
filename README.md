# PrepTrack

**AI-Enhanced DSA Practice & Spaced Repetition Platform**

PrepTrack is a full-stack platform designed to help software engineers systematically prepare for Data Structures and Algorithms interviews.

The platform combines:

- DSA problem management
- Attempt and solution tracking
- Spaced repetition
- Personal study notes
- Progress analytics
- AI-assisted code evaluation
- Secure multi-user data isolation

The project is designed with a **feature-driven backend architecture**, **CQRS-inspired read/write separation**, and a modern **standalone Angular frontend**.

---

## 🎯 Project Goals

PrepTrack is built around a simple learning loop:

```text
Discover Problem
       ↓
Attempt Problem
       ↓
Record Result
       ↓
Review Weak Areas
       ↓
Spaced Repetition
       ↓
Attempt Again
       ↓
Track Progress
```

The goal is not simply to solve more problems, but to identify weak areas and repeatedly revisit them until they become reliable interview skills.

---

# 🏗️ Architecture

PrepTrack follows a **Vertical Slice / Feature-Driven Architecture**.

The backend is organized around business capabilities rather than technical layers spread across the entire application.

```text
┌─────────────────────────────────────────────┐
│              Angular Frontend               │
│                                             │
│  Auth │ Problems │ Attempts │ Reviews       │
│       │ Dashboard │ Study                   │
└──────────────────────┬──────────────────────┘
                       │
                  REST / JSON
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              Spring Boot API                │
│                                             │
│  Core                                       │
│  ├── Security                               │
│  ├── Configuration                          │
│  └── Exception Handling                     │
│                                             │
│  Features                                   │
│  ├── Auth                                   │
│  ├── Problems                               │
│  ├── Attempts                               │
│  ├── Reviews                                │
│  └── AI                                     │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│              PostgreSQL                     │
│                                             │
│  Problem Catalog                            │
│  User Problems                              │
│  Attempts                                   │
│  Review Schedules                           │
└─────────────────────────────────────────────┘
```

---

# 🧩 Backend Architecture

The Spring Boot backend uses a **feature-driven architecture with CQRS principles**.

```text
backend/
└── src/
    └── main/
        └── java/
            └── com/suryansh/preptrack/
                ├── core/
                │   ├── config/
                │   ├── exception/
                │   └── security/
                │
                └── features/
                    ├── auth/
                    ├── problems/
                    ├── attempts/
                    ├── reviews/
                    └── ai/
```

### Core

Contains application-wide infrastructure:

- Security configuration
- JWT authentication
- Exception handling
- OpenAPI configuration
- Shared application configuration

Business logic should not be placed inside `core`.

### Features

Each feature owns its business logic, API contracts, services, commands, queries, and persistence components.

For example:

```text
features/problems/
├── controller/
├── command/
├── query/
├── domain/
├── dto/
└── service/
```

This keeps each business capability isolated and easier to maintain.

---

# 🔀 CQRS Approach

PrepTrack uses a **lightweight CQRS approach** rather than implementing a complicated CQRS framework.

### Commands

Commands modify application state.

Examples:

```text
CreateProblem
UpdateProblem
AddProblemToUser
LogAttempt
CompleteReview
ArchiveProblem
```

Write operations use transactional persistence through JPA where appropriate.

### Queries

Queries retrieve data without modifying state.

Examples:

```text
GetProblems
GetProblemDetails
GetUserProblems
GetAttempts
GetDueReviews
GetDashboardSummary
GetTopicProgress
```

Read-heavy endpoints can use `JdbcClient` and purpose-built SQL projections instead of loading full JPA entity graphs.

The objective is simple:

```text
Commands → Domain state changes
Queries  → Efficient read models
```

CQRS is used where it provides a practical benefit, not as an abstraction requirement for every operation.

---

# 🧠 Spaced Repetition

The core learning engine uses the **SM-2 spaced repetition algorithm**.

The algorithm is isolated from Spring and persistence infrastructure.

```text
Review Result
     │
     ▼
SpacedRepetitionPolicy
     │
     ├── Repetition
     ├── Interval
     ├── Ease Factor
     └── Next Review Date
              │
              ▼
       Review Schedule
```

The core policy is implemented as a pure Java component so that the mathematical behavior can be tested independently using unit and parameterized tests.

---

# 🗄️ Database Model

The Phase 2 learning domain consists of four primary concepts.

```text
catalog_problem
       │
       ├─────────────── user_problem
       │
       ├─────────────── attempt
       │
       └─────────────── review_schedule
```

### `catalog_problem`

Stores the global DSA problem catalog.

Contains information such as:

- Title
- Topic
- Difficulty
- External problem link

### `user_problem`

Represents problems that a user has added to their own study list.

Stores:

- User ownership
- Archive status
- Personal notes

### `attempt`

Stores each time a user attempts a problem.

Stores:

- Attempt date
- Time taken
- Confidence
- Assistance level
- Result
- Approach notes
- Solution code
- Programming language

Multiple attempts can exist for the same problem.

### `review_schedule`

Stores the spaced-repetition state for a user's problem.

Stores:

- Next review date
- Current interval
- Repetition count
- Ease factor
- Lapsed state
- Last reviewed timestamp
- Last attempt reference

A unique `(user_id, problem_id)` constraint ensures that a user does not accidentally receive multiple review schedules for the same problem.

---

# 🔐 Multi-User Data Isolation

PrepTrack is designed as a multi-user application.

User-owned data contains the corresponding `user_id`, while authentication/account management is handled separately from the core learning domain.

Application-level repository methods must always scope user-owned queries to the authenticated user.

For example:

```text
findAttemptById(attemptId)
```

should generally become conceptually:

```text
findAttemptByIdAndUserId(attemptId, userId)
```

This prevents one user from accessing another user's attempts, problems, or review schedules.

The authentication system establishes the current user context, while feature-level code enforces ownership.

---

# 🌐 REST API

The API is organized around business resources.

## Problems

```http
GET    /api/v1/problems
GET    /api/v1/problems/{problemId}
POST   /api/v1/problems
PUT    /api/v1/problems/{problemId}
DELETE /api/v1/problems/{problemId}
```

## User Problems

```http
GET    /api/v1/me/problems
GET    /api/v1/me/problems/{problemId}
POST   /api/v1/me/problems/{problemId}
PATCH  /api/v1/me/problems/{problemId}
DELETE /api/v1/me/problems/{problemId}
```

## Attempts

```http
POST   /api/v1/problems/{problemId}/attempts
GET    /api/v1/problems/{problemId}/attempts

GET    /api/v1/attempts/{attemptId}
PUT    /api/v1/attempts/{attemptId}
DELETE /api/v1/attempts/{attemptId}

GET    /api/v1/me/attempts
```

## Reviews

```http
GET    /api/v1/me/reviews/due
GET    /api/v1/me/reviews/upcoming
GET    /api/v1/me/reviews/history

GET    /api/v1/problems/{problemId}/review
POST   /api/v1/problems/{problemId}/reviews
```

The backend owns the spaced-repetition calculation. The frontend should not directly modify values such as:

```text
intervalDays
repetition
easeFactor
nextReviewDate
```

---

# 📊 Dashboard

The dashboard will use purpose-built queries instead of downloading the entire attempt history.

Planned endpoints include:

```http
GET /api/v1/me/dashboard
GET /api/v1/me/dashboard/progress
GET /api/v1/me/dashboard/topics
GET /api/v1/me/dashboard/activity
```

Possible dashboard metrics:

- Problems attempted
- Problems solved
- Total attempts
- Due reviews
- Overdue reviews
- Topic progress
- Difficulty distribution
- Recent activity
- Study streak

Derived statistics should generally be calculated from the underlying domain data rather than stored redundantly in the core tables.

---

# 🎨 Frontend Architecture

The Angular frontend follows the same feature-oriented philosophy.

```text
frontend/
└── src/
    └── app/
        ├── core/
        │   ├── auth/
        │   ├── http/
        │   ├── routing/
        │   └── ui/
        │
        ├── shared/
        │   ├── components/
        │   ├── directives/
        │   ├── pipes/
        │   └── utils/
        │
        ├── layout/
        │   ├── shell/
        │   ├── header/
        │   └── sidebar/
        │
        └── features/
            ├── auth/
            ├── dashboard/
            ├── problems/
            ├── attempts/
            └── reviews/
```

Angular uses:

- Standalone components
- Lazy-loaded feature routes
- Signals
- Strict TypeScript/template checking
- Feature-local models
- Feature-local services
- Centralized HTTP/auth infrastructure

The frontend does not attempt to replicate the backend's persistence architecture.

---

# 🛠️ Tech Stack

## Backend

- **Java 22**
- **Spring Boot 3.5.x**
- **Spring Security**
- **JWT**
- **Spring Data JPA / Hibernate**
- **JdbcClient**
- **PostgreSQL 16**
- **Flyway**
- **Maven**
- **Testcontainers**
- **ArchUnit**

## Frontend

- **Angular**
- **TypeScript**
- **Standalone Components**
- **Signals**
- **Angular Material**
- **Tailwind CSS**
- **Prism.js**
- **ngx-charts**

## Planned AI Layer

- Spring AI
- LLM-based code evaluation
- Structured AI responses
- BYOK API-key support
- AES-GCM credential encryption
- pgvector semantic search
- Resilience4j

---

# 📁 Project Structure

```text
preptrack/
│
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   │
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/suryansh/preptrack/
│       │   │       ├── core/
│       │   │       │   ├── config/
│       │   │       │   ├── exception/
│       │   │       │   └── security/
│       │   │       │
│       │   │       └── features/
│       │   │           ├── auth/
│       │   │           ├── problems/
│       │   │           ├── attempts/
│       │   │           ├── reviews/
│       │   │           └── ai/
│       │   │
│       │   └── resources/
│       │       ├── application.yaml
│       │       └── db/
│       │           └── migration/
│       │
│       └── test/
│
└── frontend/
    └── preptrack-ui/
        └── src/
            └── app/
                ├── core/
                ├── shared/
                ├── layout/
                └── features/
```

---

# 📅 Development Roadmap

## Phase 1 — Authentication & Security

**Status: Completed**

- [x] User registration
- [x] Login
- [x] JWT authentication
- [x] Refresh token flow
- [x] Logout
- [x] Email verification
- [x] Password reset
- [x] Change password
- [x] Current-user/session APIs
- [x] Angular authentication infrastructure
- [x] JWT interceptor
- [x] Route protection

---

## Phase 2 — Core Domain & Spaced Repetition

**Status: In Progress**

### Database

- [ ] Create `catalog_problem`
- [ ] Create `user_problem`
- [ ] Create `attempt`
- [ ] Create `review_schedule`
- [ ] Add database constraints
- [ ] Add indexes
- [ ] Add Flyway migration
- [ ] Seed initial DSA problem catalog

### Problems

- [ ] Problem catalog API
- [ ] Problem search/filtering
- [ ] Add problem to user's study list
- [ ] Archive/unarchive problem
- [ ] Personal notes

### Attempts

- [ ] Log attempt
- [ ] Record solving result
- [ ] Record confidence
- [ ] Record assistance level
- [ ] Store approach notes
- [ ] Store solution code
- [ ] Attempt history

### Spaced Repetition

- [ ] Implement pure Java `SpacedRepetitionPolicy`
- [ ] Add parameterized tests
- [ ] Create review schedule
- [ ] Calculate next review
- [ ] Handle lapsed reviews
- [ ] Due-review API
- [ ] Upcoming-review API
- [ ] Review history

### Frontend

- [ ] Problem catalog
- [ ] My Problems
- [ ] Problem details
- [ ] Attempt form
- [ ] Attempt history
- [ ] Review queue
- [ ] Dashboard foundation

---

## Phase 3 — AI Layer

**Status: Planned**

- [ ] AI code evaluation
- [ ] Structured AI responses
- [ ] BYOK API-key management
- [ ] AES-GCM credential encryption
- [ ] AI evaluation history
- [ ] pgvector integration
- [ ] Semantic search over previous approaches
- [ ] Resilience4j circuit breaker
- [ ] AI rate limiting
- [ ] Failure-isolated AI processing

The AI layer should remain **additive**.

A failure from an external AI provider must not prevent the core attempt or review state from being stored.

---

## Phase 4 — Analytics & Production Hardening

**Status: Planned**

- [ ] Advanced topic analytics
- [ ] Weak-topic detection
- [ ] Learning trends
- [ ] Advanced dashboard
- [ ] ArchUnit tenant-isolation rules
- [ ] Testcontainers integration suite
- [ ] Performance testing
- [ ] Observability
- [ ] Data export
- [ ] Production deployment
- [ ] CI/CD pipeline

---

# 🧪 Testing Strategy

PrepTrack uses multiple levels of testing.

### Unit Tests

Used for isolated business logic.

Especially important for:

```text
SpacedRepetitionPolicy
```

The spaced-repetition algorithm should be tested using parameterized test cases.

### Integration Tests

Used for:

- Repository behavior
- Database interactions
- API integration
- Security flows

PostgreSQL integration tests can use Testcontainers.

### Architecture Tests

ArchUnit can be used to enforce architectural rules such as:

```text
Feature → Feature
     ↓
should not create unwanted coupling
```

and tenant isolation rules around user-owned repositories.

---

# 🗃️ Database Migrations

Database changes are managed through **Flyway**.

Example:

```text
src/main/resources/db/migration/

V1__auth.sql
V2__core_domain.sql
V3__...
```

Migration files should be:

- Versioned
- Immutable after execution
- Small and focused
- Safe to run in deployment environments

Never modify an already-applied migration in a shared environment. Create a new migration instead.

---

# 🚀 Local Development

## Prerequisites

Install:

- Java 22
- Maven Wrapper
- Node.js
- Angular CLI
- Docker
- PostgreSQL 16

---

## Start PostgreSQL

Using Docker:

```bash
docker run \
  --name preptrack-db \
  -e POSTGRES_DB=preptrack \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:16
```

If pgvector is required for a development environment:

```bash
docker run \
  --name preptrack-db \
  -e POSTGRES_DB=preptrack \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

---

## Backend

Navigate to the backend:

```bash
cd backend/preptrack
```

Run:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Flyway will apply pending database migrations during application startup.

---

## Frontend

Navigate to:

```bash
cd frontend/preptrack-ui
```

Install dependencies:

```bash
npm install
```

Start Angular:

```bash
ng serve
```

The application will normally be available at:

```text
http://localhost:4200
```

---

# 🌿 Git Branching Strategy

Feature development follows:

```text
main
 │
 ├── feature/phase-1-auth
 │
 ├── feature/phase-2-core-domain-spaced-repetition
 │
 ├── feature/phase-3-ai
 │
 └── feature/phase-4-analytics
```

For the current work, use:

```bash
git checkout -b feature/phase-2-core-domain-spaced-repetition
```

As Phase 2 grows, commits should remain focused:

```text
feat(db): add core learning domain schema
feat(problems): add problem catalog APIs
feat(problems): add user problem management
feat(attempts): add attempt logging
feat(review): implement SM-2 policy
feat(review): add due review APIs
feat(dashboard): add learning summary
feat(frontend): add problem catalog
```

---

# 📌 Current Development Focus

The current priority is:

```text
Phase 2
   │
   ├── PostgreSQL schema
   ├── Problem catalog
   ├── User problems
   ├── Attempts
   ├── Review schedules
   ├── SM-2 policy
   └── Review APIs
```

The AI layer should **not** be allowed to delay or complicate the core learning loop.

The primary objective of Phase 2 is to establish a reliable cycle:

```text
Problem
   ↓
Attempt
   ↓
Result
   ↓
Review Schedule
   ↓
Due Review
   ↓
New Attempt
```

Once this loop is stable, the AI and advanced analytics layers can be built on top of it.

---

## 📖 Project Philosophy

PrepTrack favors:

- Simple architecture over unnecessary abstraction
- Strong domain boundaries
- Database-enforced integrity
- Explicit user ownership
- Testable business logic
- Purpose-built read queries
- Small, focused features
- Incremental development
- Production-oriented engineering practices

The project is intended to demonstrate not only that the application works, but also **why it was designed the way it was**.
