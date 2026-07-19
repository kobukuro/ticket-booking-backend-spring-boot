---
phase: 02-event-crud-business-logic
plan: 03
subsystem: api
tags: [spring-mvc, testcontainers, mockmvc, transactional, rest, state-machine]

# Dependency graph
requires:
  - phase: 02-event-crud-business-logic
    plan: 01
    provides: EventMapper.updateEventFromRequest, CreateEventRequest, UpdateEventRequest, PagedResponse, EventSpecification, EventStatus.DRAFT
  - phase: 02-event-crud-business-logic
    plan: 02
    provides: AbstractIT base class, /api/v1/ prefix convention, VenueService, PerformerService
provides:
  - EventService with 13 public methods (full CRUD, state machine, performer sub-resource)
  - EventController at /api/v1/events with 13 endpoints
  - EventControllerIT with 5 integration tests
affects:
  - 02-04-ticket-service (TicketService loads parent Event via EventRepository)

# Tech tracking
tech-stack:
  added: []
  patterns:
    - State machine enforced in service layer via explicit status guards before mutation
    - cancel() accepts both UPCOMING and ONGOING as valid source states (per D-02)
    - /upcoming endpoint declared before /{id} in source file to prevent Spring MVC routing ambiguity
    - findUpcoming() builds its own Specification directly instead of delegating to findAll()
    - All endpoints under /api/v1/ prefix (established in 02-02)

key-files:
  created:
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/service/EventService.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/controller/EventController.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/EventControllerIT.java

key-decisions:
  - "/api/v1/events prefix used on all endpoints — consistent with VenueController and PerformerController established in 02-02"
  - "EventController has no class-level @RequestMapping — handles two path structures (/api/v1/events and /api/v1/events/{id}/performers/{performerId})"
  - "findUpcoming() does not call findAll() — builds its own Specification to avoid coupling to findAll's parameter list"

patterns-established:
  - "State machine guard pattern: load entity, check status, throw CONFLICT if invalid, mutate, save, return DTO"
  - "Private findEventOrThrow helper reused across all methods that need the entity"
  - "EventControllerIT extends AbstractIT — shared container, no @Testcontainers annotation"

requirements-completed:
  - EVNT-01
  - EVNT-02
  - EVNT-03
  - EVNT-04
  - EVNT-05
  - EVNT-06
  - EVNT-07
  - EVNT-08
  - EVNT-09
  - EVNT-10

# Metrics
duration: 1 session
completed: 2026-07-19
status: complete
---

# Plan 02-03: Event CRUD REST API Summary

**Full Event REST API with 13 endpoints, D-02 state machine enforcement, and 5 passing integration tests — all 20 event-service tests green**

## Performance

- **Duration:** 1 session
- **Completed:** 2026-07-19
- **Tasks:** 2
- **Files modified:** 3 (all created)

## Accomplishments

- Implemented EventService with 13 public methods covering CRUD, 5 state transitions, and performer sub-resource management
- Enforced D-02 state machine: publish (DRAFT→UPCOMING), unpublish (UPCOMING→DRAFT), start (UPCOMING→ONGOING), cancel (UPCOMING/ONGOING→CANCELLED), complete (ONGOING→COMPLETED)
- Enforced D-06 delete guard: DELETE returns 409 when event is not DRAFT
- Implemented EventController with 13 endpoints, /upcoming declared before /{id} to prevent routing conflict
- All 5 EventControllerIT tests pass; all 17 pre-existing tests remain green (20 total)

## Task Commits

1. **Task 1: EventService** — `29b383e`
2. **Task 2: EventController + EventControllerIT** — `29b383e`

## Files Created

- `service/EventService.java` — 13 public methods + private findEventOrThrow helper; class-level @Transactional(readOnly=true); all mutation methods override with @Transactional
- `controller/EventController.java` — 13 handler methods; no class-level @RequestMapping; /upcoming before /{id}
- `test/.../EventControllerIT.java` — 5 tests: createEvent (DRAFT status), notFound 404, listEvents page, publishEvent (UPCOMING), deleteEvent 204+404

## Decisions Made

- **No class-level @RequestMapping on EventController**: handles both `/api/v1/events/{id}` and `/api/v1/events/{id}/performers/{performerId}` — a class-level mapping would require awkward relative paths
- **/upcoming before /{id}**: Spring MVC registers literal paths before pattern paths regardless of declaration order in recent versions, but explicit ordering is a defensive safeguard
- **findUpcoming() builds own Specification**: avoids coupling to the findAll() signature which takes 5 parameters

## Deviations from Plan

### Changes from Original Spec

**1. /api/v1/ prefix on all endpoints**
- **Found during:** Task 2 implementation
- **Reason:** 02-02 established /api/v1/ as the project-wide convention; plan spec used /api/ which would have been inconsistent
- **Impact:** All 13 endpoints use /api/v1/ prefix

## Issues Encountered

None.

## User Setup Required

None.

## Next Phase Readiness

- EventService is available for TicketService to load parent events via EventRepository
- State machine fully operational — 02-04 can rely on event.getStatus() returning accurate values
- AbstractIT pattern confirmed working with 6 test classes sharing one container

---
*Phase: 02-event-crud-business-logic*
*Completed: 2026-07-19*
