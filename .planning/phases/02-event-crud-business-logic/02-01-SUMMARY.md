---
phase: 02-event-crud-business-logic
plan: 01
subsystem: api, database
tags: [mapstruct, jpa, specification, flyway, jackson, dto, lombok]

# Dependency graph
requires:
  - phase: 01-foundation
    provides: Event/Venue/Performer/Ticket entities, base repositories, Flyway migrations V1–V5
provides:
  - DRAFT enum constant in EventStatus (state machine entry point)
  - V6 Flyway migration changing events.status default to DRAFT
  - EventRepository extended with JpaSpecificationExecutor
  - EventSpecification with 5 composable filter predicates
  - Global snake_case Jackson serialization via application.yml
  - PagedResponse<T> generic pagination wrapper
  - 8 request DTOs (4 Create records + 4 Update mutable classes)
  - 4 MapStruct mappers extended with null-safe partial-update methods
affects:
  - 02-02-event-service
  - 02-03-venue-performer-service
  - 02-04-ticket-service

# Tech tracking
tech-stack:
  added: []
  patterns:
    - MapStruct partial-update via @BeanMapping(nullValuePropertyMappingStrategy=IGNORE) + @MappingTarget
    - Separate Create (record) vs Update (mutable class) request DTOs for PATCH semantics
    - JPA Specification pattern for composable dynamic filtering
    - Status transition fields excluded from UpdateRequest DTOs (enforced via @Mapping ignore)

key-files:
  created:
    - event-service/src/main/resources/db/migration/V6__update_event_status_default_to_draft.sql
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/specification/EventSpecification.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/PagedResponse.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/CreateEventRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/UpdateEventRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/CreateVenueRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/UpdateVenueRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/CreatePerformerRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/UpdatePerformerRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/CreateTicketRequest.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/UpdateTicketRequest.java
  modified:
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/entity/EventStatus.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/repository/EventRepository.java
    - event-service/src/main/resources/application.yml
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/EventMapper.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/VenueMapper.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/PerformerMapper.java
    - event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/TicketMapper.java

key-decisions:
  - "UpdateEventRequest has no EventStatus field — status transitions enforced via dedicated action endpoints only (D-16)"
  - "UpdateTicketRequest allows status field — TicketStatus is not a guarded state machine, managed directly by service"
  - "venue field ignored in EventMapper.updateEventFromRequest — UUID→Entity lookup done in service layer, not mapper"
  - "performers field ignored in EventMapper.updateEventFromRequest — ManyToMany updates handled separately in service"
  - "Create DTOs use Java records (immutable); Update DTOs use mutable classes (null = skip field in PATCH)"

patterns-established:
  - "Create vs Update DTO split: records for create (all fields required), mutable Lombok class for update (null = no-op)"
  - "MapStruct partial-update: @BeanMapping(IGNORE) + explicit @Mapping(ignore=true) for id, audit timestamps, and relationship fields"
  - "Security-sensitive fields excluded at DTO layer AND enforced again at mapper layer (defence in depth)"

requirements-completed:
  - EVNT-06
  - EVNT-07
  - EVNT-09
  - EVNT-10

# Metrics
duration: ~2 sessions
completed: 2026-07-11
status: complete
---

# Plan 02-01: Shared Infrastructure Summary

**DRAFT status, JPA Specification filtering, 8 request DTOs, PagedResponse wrapper, and 4 null-safe MapStruct partial-update mappers — Wave 1 gate unblocked for Plans 02-02 through 02-04**

## Performance

- **Duration:** 2 sessions
- **Completed:** 2026-07-11
- **Tasks:** 2
- **Files modified:** 18 (7 modified + 11 created)

## Accomplishments

- Added DRAFT to EventStatus enum and V6 migration setting it as the DB default
- Built EventSpecification with 5 composable predicates (category, city, status, dateFrom, dateTo)
- Created 8 request DTOs covering Create/Update for all 4 resource families
- Extended all 4 MapStruct mappers with null-safe partial-update methods
- Configured global snake_case Jackson serialization

## Task Commits

1. **Task 1: DB Migration, Enum, Repository Extension, EventSpecification, Jackson Config** - `af05591` (feat)
2. **Task 2: Request DTOs, PagedResponse, and Mapper Partial-Update Methods** - *(pending commit)*

## Files Created/Modified

- `db/migration/V6__update_event_status_default_to_draft.sql` — changes events.status default to DRAFT
- `entity/EventStatus.java` — DRAFT added as first constant
- `repository/EventRepository.java` — extends JpaSpecificationExecutor
- `specification/EventSpecification.java` — 5 composable Specification<Event> predicates
- `resources/application.yml` — spring.jackson.property-naming-strategy: SNAKE_CASE
- `dto/PagedResponse.java` — generic pagination wrapper with static of(Page<T>) factory
- `dto/CreateEventRequest.java` — record with 6 components including venueId and performerIds
- `dto/UpdateEventRequest.java` — mutable class, no EventStatus field (D-16)
- `dto/CreateVenueRequest.java` — record with 4 components
- `dto/UpdateVenueRequest.java` — mutable class
- `dto/CreatePerformerRequest.java` — record with 1 component
- `dto/UpdatePerformerRequest.java` — mutable class
- `dto/CreateTicketRequest.java` — record with seatNumber (nullable) and price
- `dto/UpdateTicketRequest.java` — mutable class with seatNumber, price, TicketStatus
- `mapper/EventMapper.java` — updateEventFromRequest with 6 ignore targets
- `mapper/VenueMapper.java` — updateVenueFromRequest with 3 ignore targets
- `mapper/PerformerMapper.java` — updatePerformerFromRequest with 3 ignore targets
- `mapper/TicketMapper.java` — updateTicketFromRequest with 4 ignore targets, existing @Mapping preserved

## Decisions Made

- `UpdateEventRequest` excludes `EventStatus` — status transitions are only allowed via dedicated action endpoints, preventing clients from injecting arbitrary status via PATCH (D-16, T-02-01)
- `venue` and `performers` excluded from EventMapper partial-update — both require DB lookups that belong in the service layer, not the mapper
- `UpdateTicketRequest` includes `TicketStatus` — ticket status is not a guarded state machine, so direct update is acceptable

## Deviations from Plan

None — plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All Wave 1 infrastructure in place; Plans 02-02, 02-03, 02-04 are unblocked
- Service and controller layers can now inject mappers and use Specification-based filtering
- `mvn compile` and `mvn test` both pass with 0 errors

---
*Phase: 02-event-crud-business-logic*
*Completed: 2026-07-11*
