# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
./gradlew build          # compile, test, and package
./gradlew bootRun        # run the application (port 8080)
./gradlew bootTestRun    # run with test-runtime classpath
./gradlew test           # run all tests
./gradlew check          # compile + test + all verification tasks
./gradlew clean          # clean build outputs
```

**Running a single test:**
```bash
./gradlew test --tests DemoApplicationTests
./gradlew test --tests DemoApplicationTests.contextLoads
```

## Stack

- **Java 21**, **Spring Boot 4.0.6**, **Gradle 9.4.1**
- Spring MVC (`spring-boot-starter-webmvc`) for REST/web endpoints
- Spring Data JPA + H2 (file-based) for persistence
- Thymeleaf for server-side templates
- Spring DevTools enabled — app auto-restarts on file changes during `bootRun`
- JUnit 5 for testing (`@SpringBootTest` context loads test included)

## Application: Daily Takeout Coordinator

Internal tool for colleagues to coordinate daily lunch orders. Each day, users mark themselves as "Has Food" or "Ordering", propose restaurants, and vote on proposals.

### Identity
No auth. A `takeout_user` name cookie is set on first visit via `/join`. Lasts one year.

**Registration rules (one session per user):**
- On POST `/join`, the submitted name is looked up in the `Participant` table for today's `DailySession`.
- If a `Participant` row already exists for that name + today, the join is **rejected** with a validation error — the same name cannot register twice in the same day.
- Name comparison must be **case-insensitive and trimmed** (e.g. `"alice"` and `" Alice "` are the same person).
- The cookie is only written after a successful (new) registration, never on a rejected attempt.
- A `Participant` row is created atomically with the cookie write inside `TakeoutService` to prevent a race condition between two simultaneous submissions of the same name.
- The goal is to prevent one person from registering under multiple names to cast extra votes or skew participation counts.

### Data Model
- **`DailySession`** — one row per calendar day (`sessionDate` UNIQUE)
- **`Participant`** — one row per (userName, session); `status`: `HAS_FOOD` | `ORDERING`
- **`Proposal`** — restaurant suggestion per day; UNIQUE(restaurant, session)
- **`Vote`** — one vote per (userName, proposal); switching votes deletes the old one first

### Package Structure

```
com.example.teodormihai.demo/
  model/        DailySession, Participant, ParticipantStatus, Proposal, Vote
  repository/   one JpaRepository per entity
  service/      TakeoutService  (all business logic, @Transactional)
  web/          TakeoutController  (@Controller, Thymeleaf page routes)
                TakeoutApiController  (@RestController, /api/* JSON endpoints)
                CookieHelper  (read/write takeout_user cookie)
```

### Routes
| Method | Path | Description |
|---|---|---|
| GET | `/today` | Main page (redirects to `/join` if no cookie) |
| GET/POST | `/join` | Name-entry form; sets cookie on POST |
| POST | `/api/status` | Set HAS_FOOD or ORDERING for today |
| POST | `/api/proposals` | Submit a restaurant proposal |
| POST | `/api/votes` | Cast or switch vote |
| DELETE | `/api/votes/{id}` | Retract vote |
| GET | `/api/view` | Full `DailyViewDto` for current user/today |

### Frontend
`index.html` is server-rendered by Thymeleaf (initial load). `app.js` handles status/vote/proposal interactions via `fetch()` against the REST API, then patches the DOM — no full page reloads, no framework.

### Configuration Profiles
- **default** (`application.properties`): H2 file at `./data/takeout`, H2 console enabled at `/h2-console`, Thymeleaf cache off
- **prod** (`application-prod.properties`): H2 file at `/data/takeout` (mounted volume), H2 console disabled, Thymeleaf cache on; activated via `SPRING_PROFILES_ACTIVE=prod`

### Deployment
Deployed to **Koyeb** (free tier, no credit card, always-on, auto HTTPS). A `Dockerfile` at the repo root does a two-stage build (`eclipse-temurin:21-jdk-alpine` → `eclipse-temurin:21-jre-alpine`). Koyeb persistent volume is mounted at `/data`.

**Deploy steps:**
1. Push repo to GitHub
2. Sign up at [koyeb.com](https://www.koyeb.com)
3. New Service → GitHub → select repo (Koyeb auto-detects the `Dockerfile`)
4. Add a Persistent Volume mounted at `/data`
5. Set environment variable: `SPRING_PROFILES_ACTIVE=prod`
6. Deploy — free `*.koyeb.app` HTTPS URL is assigned automatically

## Project Structure

```
src/main/java/com/example/teodormihai/demo/   # application source (see packages above)
src/main/resources/
    application.properties                     # dev config
    application-prod.properties                # prod config (Koyeb)
    static/style.css, app.js                   # frontend assets
    templates/layout.html, index.html, join.html
src/test/java/com/example/teodormihai/demo/   # mirrors main package
Dockerfile                                     # two-stage build for Koyeb
```

Main entry point: `DemoApplication.java` — standard `@SpringBootApplication` bootstrap.

## Conventions

- Package root: `com.example.teodormihai.demo`
- Profile-specific config goes in `application-{profile}.properties` alongside the base file
- Test reports are written to `build/reports/tests/test/index.html`
- VS Code debug config in `.vscode/launch.json` targets `DemoApplication` on the workspace folder
