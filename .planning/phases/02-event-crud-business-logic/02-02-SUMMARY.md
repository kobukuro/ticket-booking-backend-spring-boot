---
phase: 02-event-crud-business-logic
plan: 02
subsystem: api
tags: [spring-mvc, testcontainers, mockmvc, transactional, rest]

# Dependency graph
requires:
  - phase: 02-event-crud-business-logic
    plan: 01
    provides: VenueMapper.updateVenueFromRequest, PerformerMapper.updatePerformerFromRequest, CreateVenueRequest, UpdateVenueRequest, CreatePerformerRequest, UpdatePerformerRequest, PagedResponse
provides:
  - VenueService with full CRUD (create, findAll, findById, update, delete)
  - VenueController at /api/v1/venues with 5 endpoints
  - PerformerService with full CRUD
  - PerformerController at /api/v1/performers with 5 endpoints
  - AbstractIT shared base class with single static PostgreSQL container
  - VenueControllerIT with 3 integration tests
  - PerformerControllerIT with 3 integration tests
affects:
  - 02-03-event-service
  - 02-04-ticket-service

# Tech tracking
tech-stack:
  added: []
  patterns:
    - AbstractIT base class with static container started in static initializer (no @Testcontainers) — shared across all test classes via Spring context caching
    - Service layer @Transactional(readOnly=true) at class level, @Transactional override on mutating methods
    - ResponseStatusException for 404 signals — no custom exception classes needed at this scale
    - API versioning via /api/v1/ prefix on all controllers

key-files:
  created:
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/service/VenueService.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/controller/VenueController.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/service/PerformerService.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/controller/PerformerController.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/AbstractIT.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/VenueControllerIT.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/PerformerControllerIT.java
  modified:
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/EventServiceApplicationTests.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/EventDataLayerIT.java
    - event-service/pom.xml

key-decisions:
  - "API versioning added as /api/v1/ prefix — not in original plan spec, added proactively for production readiness"
  - "AbstractIT uses static initializer (postgres.start()) instead of @Testcontainers — prevents container restart between test classes which caused CannotCreateTransactionException"
  - "Surefire configured to include *IT.java — previously IT tests were never executed by mvn test"
  - "EventServiceApplicationTests and EventDataLayerIT refactored to extend AbstractIT — single container shared across all 12 tests"

patterns-established:
  - "All controller tests extend AbstractIT — one container, one Spring context, fast execution"
  - "Controller tests use MockMvc with JSON text blocks — readable and self-contained"
  - "Service CRUD pattern: class-level @Transactional(readOnly=true), method-level @Transactional for mutations, ResponseStatusException for 404"
  - "All REST endpoints under /api/v1/ prefix"

requirements-completed:
  - EVNT-01

# Metrics
duration: 1 session
completed: 2026-07-12
status: complete
---

# Plan 02-02: Venue and Performer CRUD Summary

**Full REST CRUD for Venue (/api/v1/venues) and Performer (/api/v1/performers) with 12 passing integration tests sharing a single Testcontainers PostgreSQL instance**

## Performance

- **Duration:** 1 session
- **Completed:** 2026-07-12
- **Tasks:** 2
- **Files modified:** 10 (7 created + 3 modified)

## Accomplishments

- Implemented VenueService and PerformerService with @Transactional(readOnly=true) class-level pattern
- Implemented VenueController and PerformerController at /api/v1/ with 5 endpoints each
- Built AbstractIT base class that shares one PostgreSQL container across all test classes via Spring context caching
- Fixed pre-existing issue: IT tests were never running through `mvn test` (Surefire excluded *IT.java by default)
- All 12 tests pass in ~29s total

## Task Commits

Tasks committed together with summary:
1. **Task 1: VenueService + VenueController + VenueControllerIT** - *(this commit)*
2. **Task 2: PerformerService + PerformerController + PerformerControllerIT** - *(this commit)*

## Files Created/Modified

- `service/VenueService.java` — CRUD service, @Transactional(readOnly=true), ResponseStatusException for 404
- `controller/VenueController.java` — 5 endpoints at /api/v1/venues, POST returns 201, DELETE returns 204
- `service/PerformerService.java` — identical structural pattern to VenueService
- `controller/PerformerController.java` — 5 endpoints at /api/v1/performers
- `test/.../AbstractIT.java` — shared base class with static PostgreSQL container, no @Testcontainers
- `test/.../VenueControllerIT.java` — 3 tests: create 201, not-found 404, delete-then-404
- `test/.../PerformerControllerIT.java` — 3 tests: same contract as Venue
- `test/.../EventServiceApplicationTests.java` — refactored to extend AbstractIT
- `test/.../EventDataLayerIT.java` — refactored to extend AbstractIT
- `event-service/pom.xml` — Surefire configured to include *IT.java

## Decisions Made

- **API versioning**: Added /api/v1/ prefix to all endpoints (not in original plan). Makes the portfolio project production-ready without significant rework.
- **AbstractIT without @Testcontainers**: Using static initializer instead of @Testcontainers annotation prevents the container from being stopped between test classes. The @Testcontainers extension stops static containers after each test class, causing CannotCreateTransactionException in subsequent classes that reuse the Spring context.
- **Surefire fix**: *IT.java tests were silently excluded from `mvn test`. Fixed in pom.xml.

## Deviations from Plan

### Changes from Original Spec

**1. API versioning (/api/v1/ prefix)**
- **Found during:** Task 1 (VenueController implementation)
- **Change:** All endpoint paths use /api/v1/ prefix instead of /api/ as specified in plan
- **Reason:** User-requested improvement for production readiness
- **Impact:** Plans 02-03 and 02-04 must also use /api/v1/ prefix for consistency

**2. AbstractIT static initializer pattern**
- **Found during:** Task 1 test execution
- **Issue:** @Testcontainers stopped the container after EventDataLayerIT, causing VenueControllerIT to fail with CannotCreateTransactionException
- **Fix:** Removed @Testcontainers, used static { postgres.start() } instead
- **Verification:** All 12 tests pass

**3. Surefire *IT.java include**
- **Found during:** Task 1 test execution
- **Issue:** VenueControllerIT and EventDataLayerIT were never running through mvn test
- **Fix:** Added maven-surefire-plugin configuration in pom.xml

---

**Total deviations:** 3 (1 user-requested feature, 2 bug fixes)
**Impact on plan:** All deviations improve correctness or quality. No scope creep.

## Issues Encountered

- Spring context caching + @Testcontainers lifecycle conflict caused test failures on first run. Resolved by removing @Testcontainers in favor of static initializer.

## User Setup Required

None — no external service configuration required.

## Next Phase Readiness

- Venue and Performer CRUD fully operational and tested
- Plans 02-03 (EventService) and 02-04 (TicketService) can now reference existing venues and performers
- AbstractIT pattern is established — all future IT tests should extend it
- Note for 02-03/02-04: use /api/v1/ prefix on all new controllers

---
*Phase: 02-event-crud-business-logic*
*Completed: 2026-07-12*
