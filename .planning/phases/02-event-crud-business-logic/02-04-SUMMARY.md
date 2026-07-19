---
phase: 02-event-crud-business-logic
plan: 04
subsystem: api
tags: [spring-mvc, testcontainers, mockmvc, transactional, rest, draft-guard]

# Dependency graph
requires:
  - phase: 02-event-crud-business-logic
    plan: 01
    provides: TicketMapper.updateTicketFromRequest, CreateTicketRequest, UpdateTicketRequest, PagedResponse, TicketStatus.AVAILABLE
  - phase: 02-event-crud-business-logic
    plan: 02
    provides: AbstractIT base class, /api/v1/ prefix convention
  - phase: 02-event-crud-business-logic
    plan: 03
    provides: EventRepository (used to load parent event for DRAFT guard), EventController (used in IT to publish event for 409 test)
provides:
  - TicketRepository.findByEventId derived-query method
  - TicketService with 5 public methods (createForEvent, findByEventId, findById, update, delete)
  - TicketController at /api/v1/events/{eventId}/tickets and /api/v1/tickets/{id} with 5 endpoints
  - TicketControllerIT with 3 integration tests

# Tech tracking
tech-stack:
  added: []
  patterns:
    - DRAFT guard extracted into private requireEventInDraftStatus(Event) helper — called by create, update, delete
    - update() and delete() access parent event via ticket.getEvent() (lazy load safe inside @Transactional)
    - TicketController has no class-level @RequestMapping — handles two distinct path prefixes
    - Spring Data derived query: findByEventId maps to SELECT * FROM tickets WHERE event_id = ?

key-files:
  created:
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/service/TicketService.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/controller/TicketController.java
    - event-service/src/test/java/com/kobukuro/ticketbooking/event/TicketControllerIT.java
  modified:
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/repository/TicketRepository.java

key-decisions:
  - "requireEventInDraftStatus extracted as private helper — create, update, and delete all call it, preventing guard logic duplication"
  - "update/delete load parent event via ticket.getEvent() not via EventRepository — avoids a second DB roundtrip; lazy load is safe inside @Transactional"
  - "TicketController has no class-level @RequestMapping — manages /api/v1/events/{eventId}/tickets and /api/v1/tickets/{id} prefixes"

patterns-established:
  - "DRAFT guard pattern: requireEventInDraftStatus(event) throws 409 CONFLICT — mirrors state machine guard pattern from EventService"
  - "TicketControllerIT extends AbstractIT — maintains single shared container across all 7 test classes"

requirements-completed:
  - EVNT-01

# Metrics
duration: 1 session
completed: 2026-07-19
status: complete
---

# Plan 02-04: Ticket CRUD REST API Summary

**Ticket resource API with DRAFT-event guard on all mutations — 3 integration tests pass; full suite at 20 tests green**

## Performance

- **Duration:** 1 session
- **Completed:** 2026-07-19
- **Tasks:** 2
- **Files modified:** 4 (3 created + 1 modified)

## Accomplishments

- Extended TicketRepository with findByEventId derived-query method
- Implemented TicketService with DRAFT guard enforced on create, update, and delete
- Implemented TicketController with 5 endpoints across two path prefixes
- All 3 TicketControllerIT tests pass including the 409 guard test; full 20-test suite green

## Task Commits

1. **Task 1: TicketRepository + TicketService** — `76b5f53`
2. **Task 2: TicketController + TicketControllerIT** — `76b5f53`

## Files Created/Modified

- `repository/TicketRepository.java` — added Page<Ticket> findByEventId(UUID eventId, Pageable pageable)
- `service/TicketService.java` — 5 public methods + 3 private helpers; class-level @Transactional(readOnly=true)
- `controller/TicketController.java` — 5 endpoints, no class-level @RequestMapping
- `test/.../TicketControllerIT.java` — 3 tests: create for DRAFT (201), create for non-DRAFT (409), get notFound (404)

## Decisions Made

- **requireEventInDraftStatus as private helper**: create, update, and delete all need the same guard — extracting it prevents duplication and makes the rule easy to find and change
- **ticket.getEvent() for lazy load**: update() and delete() already have the ticket loaded; accessing the parent event through it avoids a redundant EventRepository.findById call; the @Transactional context keeps the session open so the lazy load succeeds

## Deviations from Plan

### Changes from Original Spec

**1. /api/v1/ prefix on all endpoints**
- **Reason:** Consistent with /api/v1/ convention established in 02-02; plan spec used /api/ prefix

**2. TicketControllerIT extends AbstractIT instead of declaring own container**
- **Reason:** AbstractIT was established in 02-02 specifically to share one container across all test classes; re-declaring a container in TicketControllerIT would break Spring context caching and cause CannotCreateTransactionException

## Issues Encountered

None.

## User Setup Required

None.

## Next Phase Readiness

- Phase 02 complete — all 4 plans executed, 20 tests passing
- Event and Ticket resources fully operational with business rule enforcement
- AbstractIT supports 7 test classes sharing one PostgreSQL container

---
*Phase: 02-event-crud-business-logic*
*Completed: 2026-07-19*
