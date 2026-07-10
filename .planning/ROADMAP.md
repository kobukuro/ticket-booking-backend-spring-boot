# Roadmap: Ticket Booking Backend (Event Service)

## Overview

Build the event-service as the first microservice in a ticket booking backend. Work proceeds in horizontal layers: first establish the project scaffold, database schema, and data mapping infrastructure; then build all REST API endpoints with business logic for event management; finally layer on input validation, consistent error handling, and API documentation. Each phase delivers a verifiable technical layer that the next phase builds upon.

## Phases

**Phase Numbering:**

- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Project Scaffold & Data Layer** - Multi-module Maven structure, Flyway migrations, JPA entities, and MapStruct DTO mapping
- [ ] **Phase 2: Event CRUD & Business Logic** - All event management endpoints with filtering, pagination, and status management
- [ ] **Phase 3: API Quality & Documentation** - Bean Validation, global error handling, and Swagger UI documentation

## Phase Details

### Phase 1: Project Scaffold & Data Layer

**Goal**: A compilable multi-module Maven project with a working database layer -- entities, migrations, repositories, and DTO mapping all wired and verified
**Depends on**: Nothing (first phase)
**Requirements**: INFR-01, INFR-02, INFR-04
**Success Criteria** (what must be TRUE):

  1. Running `mvn clean install` from the project root compiles both parent POM and event-service module without errors
  2. Starting event-service connects to PostgreSQL and Flyway applies the event table migration automatically
  3. MapStruct generates DTO-to-entity and entity-to-DTO mappers at compile time without manual mapping code
  4. The event entity uses UUID primary keys and includes all fields: name, description, category, venue, city, date/time, ticket count, price, and status

**Plans**: 2 plans
**Wave 1**

- [ ] 01-01-PLAN.md — Multi-module Maven scaffold, wrapper, Spring Boot app, application.yml, docker-compose (INFR-01)

**Wave 2** *(blocked on Wave 1 completion)*

- [ ] 01-02-PLAN.md — Flyway V1 migration, Event entity + enums, repository, DTO, MapStruct mapper, Testcontainers verification (INFR-02, INFR-04)

### Phase 2: Event CRUD & Business Logic

**Goal**: API consumers can create, read, update, delete, filter, and paginate events through REST endpoints on port 8081
**Depends on**: Phase 1
**Requirements**: EVNT-01, EVNT-02, EVNT-03, EVNT-04, EVNT-05, EVNT-06, EVNT-07, EVNT-08, EVNT-09, EVNT-10
**Success Criteria** (what must be TRUE):

  1. Sending a POST to `/api/events` with valid event data creates an event and returns the created resource with a UUID
  2. Sending GET requests to `/api/events` returns a paginated list, and adding `category`, `city`, or `status` query parameters filters results correctly
  3. Sending a PATCH to `/api/events/{id}` with partial fields updates only those fields and returns the updated event
  4. Sending DELETE to `/api/events/{id}` removes the event and subsequent GET for that ID returns 404
  5. Events have status values (UPCOMING, ONGOING, COMPLETED, CANCELLED) and GET `/api/events/upcoming` returns only future events sorted by date

**Plans**: 4 plans

**Wave 1** *(no dependencies)*

- [ ] 02-01-PLAN.md — Shared infrastructure: V6 migration, EventStatus DRAFT, EventRepository JpaSpecificationExecutor, EventSpecification, request DTOs (8), PagedResponse, mapper partial-update methods, Jackson SNAKE_CASE (EVNT-06, EVNT-07, EVNT-09, EVNT-10)

**Wave 2** *(blocked on Wave 1 completion — all three run in parallel)*

- [ ] 02-02-PLAN.md — Venue + Performer full CRUD API: VenueService, VenueController, PerformerService, PerformerController with integration tests (EVNT-01)
- [ ] 02-03-PLAN.md — Event full CRUD API: EventService (13 methods), EventController (13 endpoints), state machine, filtering, upcoming endpoint (EVNT-01 through EVNT-10)
- [ ] 02-04-PLAN.md — Ticket API with DRAFT-event guard: TicketRepository, TicketService, TicketController with integration tests (EVNT-01)

### Phase 3: API Quality & Documentation

**Goal**: The API rejects bad input with clear error messages, handles errors consistently, and is fully browsable through Swagger UI
**Depends on**: Phase 2
**Requirements**: QUAL-01, QUAL-02, QUAL-03, INFR-03
**Success Criteria** (what must be TRUE):

  1. Sending a POST with missing required fields (e.g., no event name) returns 400 with a JSON body listing each invalid field and its error message
  2. All error responses share the same JSON structure: status code, message, timestamp, and field-level errors when applicable
  3. Requesting a non-existent event ID returns 404 with a descriptive message (not a stack trace or generic Spring error page)
  4. Opening `/swagger-ui.html` (or `/swagger-ui/index.html`) in a browser displays interactive API documentation for all event endpoints

**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Project Scaffold & Data Layer | 0/0 | Not started | - |
| 2. Event CRUD & Business Logic | 0/0 | Not started | - |
| 3. API Quality & Documentation | 0/0 | Not started | - |
