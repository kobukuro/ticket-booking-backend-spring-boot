# Phase 02: Event CRUD & Business Logic — Research

**Researched:** 2026-07-10
**Domain:** Spring MVC REST API, Spring Data JPA Specification, MapStruct partial update, event state machine
**Confidence:** HIGH (standard Spring Boot patterns, existing codebase fully read)

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** EventStatus enum gains DRAFT (in addition to UPCOMING, ONGOING, COMPLETED, CANCELLED)
- **D-02:** Status transitions: DRAFT ↔ UPCOMING (reversible), UPCOMING → ONGOING or CANCELLED, ONGOING → COMPLETED or CANCELLED, COMPLETED and CANCELLED are terminal
- **D-03:** Status transitions exposed as dedicated action endpoints (POST /api/events/{id}/publish, /unpublish, /cancel, /start, /complete) — NOT via PATCH
- **D-04:** Full CRUD for Event, Venue, Performer
- **D-05:** Full CRUD for Ticket, but create/update/delete restricted to events in DRAFT status only; read (GET) always allowed
- **D-06:** Event delete only allowed when event is in DRAFT status
- **D-07:** POST /api/events accepts `venueId` (UUID) and `performerIds` (UUID array) in request body
- **D-08:** Event response includes full venue details and full performer details (not just IDs)
- **D-09:** Tickets managed via /api/events/{id}/tickets and /api/tickets/{id}
- **D-10:** Event list supports filters: category, city, status, dateFrom, dateTo as query parameters
- **D-11:** Pagination: offset-based (?page=0&size=20) using Spring Data Pageable
- **D-12:** Default sort: event_date_time ascending (nearest event first)
- **D-13:** Event update uses PATCH (partial update) — only provided fields are updated
- **D-14:** Performer membership via POST /api/events/{id}/performers/{performerId} and DELETE /api/events/{id}/performers/{performerId}
- **D-15:** Venue update in PATCH body via venueId field
- **D-16:** PATCH on Event does NOT accept status field — use action endpoints instead

### Claude's Discretion

- Default page size (recommend 20)
- Exact request/response field names (follow Java naming conventions → snake_case via Jackson)
- HTTP status codes for each endpoint (standard REST conventions)
- Whether to support GET /api/events/upcoming as a shortcut (deferred from decisions)

### Deferred Ideas (OUT OF SCOPE)

- GET /api/events/upcoming shortcut endpoint — deferred to Claude's discretion during planning
- Date range filter UI/UX details — Claude decides query param naming (dateFrom/dateTo vs startDate/endDate)
- Online events (virtual venue) — deferred to future phase
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| EVNT-01 | Organizer can create an event with name, description, category, venue, date/time, ticket count, and price | CreateEventRequest DTO → EventService.create() → EventRepository.save() |
| EVNT-02 | Anyone can view a list of all events | GET /api/events → EventRepository.findAll(spec, pageable) |
| EVNT-03 | Anyone can view a single event by ID | GET /api/events/{id} → EventRepository.findById() |
| EVNT-04 | Organizer can update an existing event (partial update) | PATCH /api/events/{id} with MapStruct @BeanMapping IGNORE_NULL strategy |
| EVNT-05 | Organizer can delete an event | DELETE /api/events/{id} restricted to DRAFT status (D-06) |
| EVNT-06 | Anyone can filter events by category | EventSpec.hasCategory(category) combined with Specification.and() |
| EVNT-07 | Anyone can filter events by city | EventSpec.hasCity(city) via venue join in Specification |
| EVNT-08 | Anyone can view upcoming events (sorted by date) | GET /api/events/upcoming — dedicated endpoint returning UPCOMING events sorted by eventDateTime |
| EVNT-09 | Event has status management (UPCOMING, ONGOING, COMPLETED, CANCELLED) | Action endpoints + DRAFT added; state machine enforced in service |
| EVNT-10 | Event listings support pagination | Pageable parameter on findAll(); custom PagedResponse<T> wrapper returned |
</phase_requirements>

---

## Summary

Phase 2 builds on a complete data layer (entities, repositories, DTOs, mappers from Phase 1) to add the full service and controller layers. All required Spring Boot infrastructure is already present in the POM — no new Maven dependencies are needed. The work is structured into four resource families: Event (with its state machine and filtering), Venue, Performer, and Ticket.

The two most technically demanding areas are (1) dynamic filtering via Spring Data JPA Specification and (2) PATCH partial update via MapStruct's null-property-ignore strategy. Both are standard Spring Boot patterns with clear implementation paths. The event status state machine is simple enough to implement with inline service-layer checks — no state machine library is warranted.

A critical database finding: the V3 migration has no CHECK constraint on the `status` column, so adding DRAFT to the Java enum requires only changing the column default via a minimal V6 migration. No constraint modification is needed.

**Primary recommendation:** Implement in resource layers (service + controller per resource), with a shared EventSpecification class for filtering and a generic PagedResponse record for pagination. Use MapStruct's `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` with `@MappingTarget` for all PATCH operations.

---

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| REST endpoint routing | API / Backend (Spring MVC Controller) | — | All HTTP routing is handled by @RestController beans in the API tier |
| Business rules (status transitions, DRAFT guards) | API / Backend (Service Layer) | — | Stateless business logic belongs in @Service, not controller or repository |
| Dynamic filtering | Database / Storage (JPA Specification → SQL) | API / Backend (Specification builder) | Filter predicates are SQL WHERE clauses; the builder lives in service/specification package |
| Pagination and sorting | Database / Storage (Spring Data Pageable) | API / Backend (response wrapper) | SQL LIMIT/OFFSET is generated by Spring Data; wrapper is assembled in service |
| DTO mapping | API / Backend (MapStruct) | — | Compile-time mapping; not a DB or network concern |
| Schema migration (DRAFT default) | Database / Storage (Flyway V6) | — | Column default change is a DB-level change |

---

## Standard Stack

### Core (all already present in event-service/pom.xml — no new dependencies required)

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| spring-boot-starter-web | Managed by Boot 3.3.13 | Spring MVC REST controllers, Jackson JSON | Already present; provides @RestController, ResponseEntity, Pageable resolver |
| spring-boot-starter-data-jpa | Managed by Boot 3.3.13 | JPA repositories, JpaSpecificationExecutor | Already present; provides Specification<T> and Pageable |
| mapstruct | 1.6.3 | Compile-time DTO mapping including partial update | Already present; @BeanMapping + @MappingTarget covers PATCH |
| lombok | 1.18.34+ (managed) | Boilerplate reduction on request DTOs | Already present |

### No New Dependencies Required

All capabilities needed for Phase 2 are covered by Phase 1's dependency set. The service and controller layers use only what is already declared in `event-service/pom.xml`.

**Installation:** None required. `spring-boot-starter-validation` (Bean Validation) is NOT added in this phase — it belongs to Phase 3 (QUAL-01, QUAL-02).

---

## Package Legitimacy Audit

No new external packages are introduced in this phase. All libraries were validated and installed in Phase 1.

**Packages removed due to SLOP verdict:** none
**Packages flagged as suspicious (SUS):** none

---

## Architecture Patterns

### System Architecture Diagram

```
HTTP Request (port 8081)
        |
        v
+-------------------+
|   Spring MVC      |  @RestController
|   Controller      |  (EventController, VenueController,
|   Layer           |   PerformerController, TicketController)
+-------------------+
        |
        | calls
        v
+-------------------+
|   Service Layer   |  @Service @Transactional
|                   |  (EventService, VenueService,
|                   |   PerformerService, TicketService)
|                   |  -- status machine checks
|                   |  -- DRAFT guards
+-------------------+
        |
        +---> EventSpecification (Specification<Event> builder)
        |     for dynamic filter composition
        |
        v
+-------------------+
|  Spring Data JPA  |  JpaRepository + JpaSpecificationExecutor
|  Repository Layer |  findAll(Specification, Pageable)
+-------------------+
        |
        v
+-------------------+
|   PostgreSQL 16   |  event-service DB
|   (port 5432)     |  Flyway-managed schema (V1–V6)
+-------------------+

Mapping:
  Request JSON → @RequestBody CreateXxxRequest → Service → Entity → JPA
  Entity → MapStruct → XxxDto → @ResponseBody → Response JSON
  Page<Entity> → page.map(mapper::toDto) → PagedResponse<XxxDto>
```

### Recommended Project Structure

```
event-service/src/main/java/com/kobukuro/ticketbooking/event/
├── controller/
│   ├── EventController.java
│   ├── VenueController.java
│   ├── PerformerController.java
│   └── TicketController.java
├── service/
│   ├── EventService.java
│   ├── VenueService.java
│   ├── PerformerService.java
│   └── TicketService.java
├── dto/
│   ├── EventDto.java              (existing — response)
│   ├── VenueDto.java              (existing — response)
│   ├── PerformerDto.java          (existing — response)
│   ├── TicketDto.java             (existing — response)
│   ├── PagedResponse.java         (new — generic pagination wrapper)
│   ├── CreateEventRequest.java    (new — POST body)
│   ├── UpdateEventRequest.java    (new — PATCH body, nullable fields)
│   ├── CreateVenueRequest.java    (new)
│   ├── UpdateVenueRequest.java    (new)
│   ├── CreatePerformerRequest.java (new)
│   ├── UpdatePerformerRequest.java (new)
│   ├── CreateTicketRequest.java   (new)
│   └── UpdateTicketRequest.java   (new)
├── specification/
│   └── EventSpecification.java    (new — Specification<Event> builder)
├── entity/          (existing — no changes except EventStatus.DRAFT)
├── repository/      (existing — EventRepository gains JpaSpecificationExecutor)
└── mapper/          (existing — all mappers gain updateXxx(@MappingTarget) methods)

event-service/src/main/resources/db/migration/
└── V6__update_event_status_default_to_draft.sql   (new)
```

### Pattern 1: Spring MVC REST Controller

**What:** `@RestController` with resource-scoped `@RequestMapping`, using `ResponseEntity<T>` for HTTP status control.
**When to use:** Every controller in this phase.

```java
// [ASSUMED] Standard Spring MVC REST pattern
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody CreateEventRequest request) {
        EventDto created = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<EventDto>> listEvents(
            @RequestParam(required = false) EventCategory category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(category, city, status, dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEvent(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable UUID id,
                                                @RequestBody UpdateEventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Status transition endpoints
    @PostMapping("/{id}/publish")
    public ResponseEntity<EventDto> publish(@PathVariable UUID id) {
        return ResponseEntity.ok(eventService.publish(id));
    }
}
```

### Pattern 2: Spring Data JPA Specification for Dynamic Filtering

**What:** `Specification<T>` composable predicates combined with `and()`. Repository extends `JpaSpecificationExecutor<T>`.
**When to use:** EventRepository for `GET /api/events` with optional category/city/status/date filters.

```java
// [ASSUMED] Standard Spring Data JPA Specification pattern
// Step 1: Repository extends JpaSpecificationExecutor
public interface EventRepository extends JpaRepository<Event, UUID>,
        JpaSpecificationExecutor<Event> {
}

// Step 2: Static factory methods in EventSpecification
public class EventSpecification {

    public static Specification<Event> hasCategory(EventCategory category) {
        return (root, query, cb) ->
            category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Event> hasCity(String city) {
        return (root, query, cb) ->
            city == null ? null : cb.equal(root.join("venue").get("city"), city);
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime from) {
        return (root, query, cb) ->
            from == null ? null : cb.greaterThanOrEqualTo(root.get("eventDateTime"), from);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime to) {
        return (root, query, cb) ->
            to == null ? null : cb.lessThanOrEqualTo(root.get("eventDateTime"), to);
    }
}

// Step 3: Service composes specs and calls repository
Specification<Event> spec = Specification
    .where(EventSpecification.hasCategory(category))
    .and(EventSpecification.hasCity(city))
    .and(EventSpecification.hasStatus(status))
    .and(EventSpecification.eventDateAfter(dateFrom))
    .and(EventSpecification.eventDateBefore(dateTo));

Page<Event> page = eventRepository.findAll(spec, pageable);
```

**Important:** When `hasCity()` joins the venue relation, JPA generates a JOIN in the content query AND the count query. This works correctly since it is a `ManyToOne` (not a collection join). No special handling is needed.

### Pattern 3: PATCH Partial Update with MapStruct

**What:** Request DTO with nullable fields (null = "not provided, skip"). MapStruct mapper method with `@MappingTarget` and `NullValuePropertyMappingStrategy.IGNORE`.
**When to use:** All four PATCH endpoints (Event, Venue, Performer, Ticket).

```java
// [ASSUMED] Standard MapStruct partial update pattern

// Request DTO — use a class (not a record) so fields can be null to mean "not provided"
@Getter
@Setter
@NoArgsConstructor
public class UpdateEventRequest {
    private String name;               // null = not provided, skip
    private String description;        // null = not provided, skip
    private EventCategory category;    // null = not provided, skip
    private UUID venueId;              // null = not provided, skip (D-15)
    private LocalDateTime eventDateTime; // null = not provided, skip
    // NOTE: no 'status' field (D-16) — use action endpoints
}

// MapStruct mapper — add update method alongside existing toDto/toEntity
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {VenueMapper.class, PerformerMapper.class})
public interface EventMapper {

    EventDto toDto(Event event);
    Event toEntity(EventDto eventDto);

    // NEW: partial update — ignores null source fields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "performers", ignore = true)
    @Mapping(target = "venue", ignore = true)   // venue resolved separately via venueId
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEventFromRequest(UpdateEventRequest request, @MappingTarget Event event);
}

// Service layer PATCH flow:
// 1. Load entity
// 2. If venueId provided, resolve venue from VenueRepository
// 3. Call mapper.updateEventFromRequest(request, event)
// 4. If venueId non-null, set event.setVenue(resolvedVenue)
// 5. eventRepository.save(event)
```

### Pattern 4: Event Status State Machine

**What:** Inline service-layer checks for allowed transitions — no state machine library needed.
**When to use:** All five action endpoints (publish, unpublish, start, cancel, complete).

```java
// [ASSUMED] Inline state machine with ResponseStatusException for HTTP semantics
public EventDto publish(UUID id) {
    Event event = findEventOrThrow(id);
    if (event.getStatus() != EventStatus.DRAFT) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Event must be in DRAFT status to publish. Current status: " + event.getStatus());
    }
    event.setStatus(EventStatus.UPCOMING);
    return eventMapper.toDto(eventRepository.save(event));
}

// Shared helper (used by all action endpoints and DRAFT-guarded operations)
private Event findEventOrThrow(UUID id) {
    return eventRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Event not found: " + id));
}
```

**Transition map (enforced per action endpoint):**

| Endpoint | Required current status | Target status |
|----------|------------------------|---------------|
| /publish | DRAFT | UPCOMING |
| /unpublish | UPCOMING | DRAFT |
| /start | UPCOMING | ONGOING |
| /cancel | UPCOMING or ONGOING | CANCELLED |
| /complete | ONGOING | COMPLETED |

### Pattern 5: Pagination Response Wrapper

**What:** Generic `PagedResponse<T>` record assembled from `Page<T>` in the service layer.
**When to use:** All list endpoints that return paginated results.

```java
// [ASSUMED] Standard pagination wrapper pattern
public record PagedResponse<T>(
    List<T> content,
    int totalPages,
    long totalElements,
    int number,
    int size
) {
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(
            page.getContent(),
            page.getTotalPages(),
            page.getTotalElements(),
            page.getNumber(),
            page.getSize()
        );
    }
}

// Service usage:
Page<EventDto> dtoPage = eventRepository.findAll(spec, pageable).map(eventMapper::toDto);
return PagedResponse.of(dtoPage);
```

### Pattern 6: Default Pageable Configuration

**What:** Configure default sort in the controller so callers who omit `?sort=` get `eventDateTime,asc` automatically.
**When to use:** Event list endpoint to fulfill D-12.

```java
// [ASSUMED] Spring MVC @PageableDefault annotation
@GetMapping
public ResponseEntity<PagedResponse<EventDto>> listEvents(
        // ... filter params ...
        @PageableDefault(size = 20, sort = "eventDateTime", direction = Sort.Direction.ASC)
        Pageable pageable) {
    // ...
}
```

### Pattern 7: Flyway V6 Migration for DRAFT Default

**What:** Change the `status` column default from `'UPCOMING'` to `'DRAFT'`. No CHECK constraint exists in V3.
**When to use:** Must be applied before service layer creates events (which start as DRAFT).

```sql
-- V6__update_event_status_default_to_draft.sql
-- [VERIFIED: read V3__create_events_table.sql] No CHECK constraint exists on status column.
-- Only need to update the column default value.
ALTER TABLE events ALTER COLUMN status SET DEFAULT 'DRAFT';
```

### Pattern 8: GET /api/events/upcoming Endpoint (Claude's Discretion)

**Recommendation:** Implement as a dedicated endpoint. The ROADMAP.md success criterion 5 explicitly names this URL. A dedicated endpoint is clearer than relying on callers to construct `?status=UPCOMING&sort=eventDateTime,asc`.

```java
// [ASSUMED]
@GetMapping("/upcoming")
public ResponseEntity<PagedResponse<EventDto>> upcomingEvents(
        @PageableDefault(size = 20, sort = "eventDateTime", direction = Sort.Direction.ASC)
        Pageable pageable) {
    return ResponseEntity.ok(eventService.findUpcoming(pageable));
}

// Service:
public PagedResponse<EventDto> findUpcoming(Pageable pageable) {
    Specification<Event> spec = Specification
        .where(EventSpecification.hasStatus(EventStatus.UPCOMING))
        .and(EventSpecification.eventDateAfter(LocalDateTime.now()));
    Page<EventDto> page = eventRepository.findAll(spec, pageable).map(eventMapper::toDto);
    return PagedResponse.of(page);
}
```

### Pattern 9: Performer Sub-Resource Endpoints

**What:** Add/remove from the `event_performers` join table via the Event entity's `performers` Set. No new entity.
**When to use:** POST /api/events/{id}/performers/{performerId} and DELETE counterpart.

```java
// [ASSUMED] Operate on the in-memory Set, then save the Event
public EventDto addPerformer(UUID eventId, UUID performerId) {
    Event event = findEventOrThrow(eventId);
    Performer performer = performerRepository.findById(performerId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Performer not found: " + performerId));
    event.getPerformers().add(performer);
    return eventMapper.toDto(eventRepository.save(event));
}
```

### Anti-Patterns to Avoid

- **Returning entities directly:** Never return JPA entity objects from controllers. Always map to DTOs via MapStruct. The Event entity has `@ManyToMany performers` which are lazily loaded — serialising the entity outside a transaction causes `LazyInitializationException`.
- **Checking status in controller:** Status transition validation belongs in the service layer, not the controller. Controllers should only marshal/unmarshal HTTP.
- **Calling `findAll()` without Specification:** For list endpoints, always pass a `Specification` (even if all filters are null — Spring Data handles null predicates gracefully). Never add a separate `findAll()` vs `findAllFiltered()` bifurcation.
- **Using `@Query` for dynamic filters:** JPQL `@Query` methods require one method per filter combination. `JpaSpecificationExecutor` composes at runtime without combinatorial method explosion.
- **PATCH accepting `status` field:** Decision D-16 explicitly forbids this. The mapper must ignore `status` in `updateEventFromRequest()`.

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Dynamic WHERE clause composition | Multiple repository methods (findByCategory, findByCategoryAndCity, ...) | `JpaSpecificationExecutor` + `Specification<T>` | N filter params = 2^N method combinations needed; Specification composes at runtime |
| PATCH null-field detection | Manual null checks in service (`if (req.getName() != null) event.setName(req.getName())`) | MapStruct `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` | Brittle, verbose, easy to miss a field; MapStruct generates it at compile time |
| UUID ↔ String conversion | Manual `UUID.fromString()` in controller | Spring MVC built-in type conversion | @PathVariable UUID id auto-converts; no manual parsing needed |
| Pagination response shape | Manual field extraction from Page | `PagedResponse.of(page)` static factory | One place to change if the API contract evolves |
| Status 404 for missing entities | Try/catch around `findById()` | `findById().orElseThrow(() -> new ResponseStatusException(NOT_FOUND, ...))` | Spring translates ResponseStatusException to the correct HTTP status automatically |

**Key insight:** The three hardest things in this domain (dynamic query composition, partial update field skipping, UUID binding) are already solved by Spring Data, MapStruct, and Spring MVC respectively. Do not re-implement them.

---

## Common Pitfalls

### Pitfall 1: LazyInitializationException When Mapping Event to EventDto

**What goes wrong:** `eventMapper.toDto(event)` is called outside a transaction, or after the JPA session closes. The mapper accesses `event.getPerformers()` (a lazy `Set<Performer>`), which triggers a proxy load — but the session is gone.

**Why it happens:** The `performers` collection on `Event` uses the default lazy fetch strategy (`@ManyToMany` is lazy by default). The EventDto includes a full `Set<PerformerDto>`.

**How to avoid:** Annotate all service methods that return mapped DTOs with `@Transactional(readOnly = true)`. The session remains open during the mapping call.

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // keeps session open for lazy collection access
public class EventService {

    public EventDto findById(UUID id) {
        Event event = findEventOrThrow(id); // session still open here
        return eventMapper.toDto(event);    // performers loaded within same session
    }

    @Transactional  // mutations use read-write transaction
    public EventDto create(CreateEventRequest request) { ... }
}
```

**Warning signs:** `org.hibernate.LazyInitializationException: failed to lazily initialize a collection` in logs.

### Pitfall 2: N+1 Queries on GET /api/events (List Endpoint)

**What goes wrong:** Fetching a page of 20 events triggers 20 additional SELECT queries — one per event — to load each event's performers.

**Why it happens:** Spring Data JPA's `findAll(spec, pageable)` loads events in one query, but each `event.getPerformers()` call during mapping triggers a separate query.

**How to avoid:** Use a JPQL query hint or fetch join for the list endpoint. The cleanest approach for this project: accept the N+1 for now (Phase 2 has no performance requirements), and note it for future optimization. Alternatively, use `@EntityGraph(attributePaths = {"performers", "venue"})` on the repository.

```java
// Option A: Accept N+1 in Phase 2 (simplest for now)
// The load is bounded by page size (20 by default) and acceptable for a learning project.

// Option B (if performance matters later): EntityGraph
// @EntityGraph(attributePaths = {"performers", "venue"})
// Page<Event> findAll(Specification<Event> spec, Pageable pageable);
// NOTE: @EntityGraph does NOT work with JpaSpecificationExecutor in Spring Data <3.0.
// For Boot 3.3.x (Hibernate 6.x), it does work.
```

**Warning signs:** `select ... from event_performers where event_id=?` repeating 20 times in SQL log.

### Pitfall 3: Specification with Join Causes Duplicate Rows on COUNT Query

**What goes wrong:** When `hasCity()` uses `root.join("venue")`, the generated count query may not correctly de-duplicate. This is only a risk for `@OneToMany` or `@ManyToMany` joins, NOT `@ManyToOne` (which is the venue relationship here).

**Why it matters:** `Event.venue` is `@ManyToOne` — one venue per event. The join never multiplies rows. This pitfall does NOT apply to city filtering in this schema.

**How to avoid (for future @ManyToMany filters):** Use `root.joinSet("performers", JoinType.LEFT)` + `query.distinct(true)` in the Specification, and provide a separate count specification without the join.

**Warning signs (if this were a ManyToMany filter):** `totalElements` in pagination response returns duplicate counts.

### Pitfall 4: PATCH Request DTO Defined as Java Record

**What goes wrong:** `record UpdateEventRequest(String name, ...)` initializes all fields to `null` when not provided in JSON. This is correct. BUT if the DTO is a record, MapStruct cannot use `@MappingTarget` on it (records are immutable — no setters).

**Why it happens:** MapStruct's partial update (`@MappingTarget`) requires mutable target objects (entity). The request DTO can be a record (it's the source). The entity (target) must have setters — which it does via `@Setter` Lombok annotation.

**How to avoid:** Request DTOs can be Java records (immutable is fine for the source). Only the entity (MappingTarget) needs setters. The existing entities all have `@Setter` (Lombok).

**Warning signs:** MapStruct compilation error: `"no property write accessor found for type ..."`.

### Pitfall 5: Concurrent EventStatus CHECK in V6 Migration

**What goes wrong:** V6 migration adds `DEFAULT 'DRAFT'` to the status column. BUT if existing rows have status values not in the new set, the DB doesn't care (there's no CHECK constraint). The risk is running V6 before updating the Java enum — the service would reject 'DRAFT' from the DB.

**Why it happens:** V3 schema has no CHECK constraint, so the DB accepts any string value. The Java enum is the validation layer.

**How to avoid:** In Phase 2, update the Java `EventStatus` enum to add `DRAFT` in the same commit as V6 migration. The Flyway migration and enum change must be in the same deployable unit.

### Pitfall 6: Ticket DRAFT Guard Requires Fetching Parent Event

**What goes wrong:** `DELETE /api/tickets/{id}` needs to verify the ticket's parent event is in DRAFT status (D-05). But the ticket endpoint only receives the ticket ID, not the event ID.

**Why it happens:** The API is designed with a flat ticket endpoint (`/api/tickets/{id}`) for convenience, but the business rule is on the parent event.

**How to avoid:** In `TicketService`, after loading the ticket by ID, check `ticket.getEvent().getStatus() == EventStatus.DRAFT`. The `Ticket.event` relation is `@ManyToOne(fetch = FetchType.LAZY)`, so `ticket.getEvent()` triggers a DB load — but within a `@Transactional` service method, this is safe.

---

## Code Examples

### Create Event Request DTO and Service Method

```java
// [ASSUMED] CreateEventRequest — Java record is fine for immutable request body
public record CreateEventRequest(
    String name,
    String description,
    EventCategory category,
    UUID venueId,
    List<UUID> performerIds,
    LocalDateTime eventDateTime
) {}

// EventService.create()
@Transactional
public EventDto create(CreateEventRequest request) {
    Venue venue = venueRepository.findById(request.venueId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Venue not found: " + request.venueId()));

    Event event = Event.builder()
        .name(request.name())
        .description(request.description())
        .category(request.category())
        .venue(venue)
        .eventDateTime(request.eventDateTime())
        .status(EventStatus.DRAFT)   // D-01: new events start as DRAFT
        .build();

    if (request.performerIds() != null) {
        List<Performer> performers = performerRepository.findAllById(request.performerIds());
        event.getPerformers().addAll(performers);
    }

    return eventMapper.toDto(eventRepository.save(event));
}
```

### Complete EventSpecification Class

```java
// [ASSUMED]
public class EventSpecification {

    private EventSpecification() {}

    public static Specification<Event> hasCategory(EventCategory category) {
        return (root, query, cb) ->
            category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Event> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null) return null;
            Join<Event, Venue> venue = root.join("venue", JoinType.INNER);
            return cb.equal(venue.get("city"), city);
        };
    }

    public static Specification<Event> hasStatus(EventStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Event> eventDateAfter(LocalDateTime from) {
        return (root, query, cb) ->
            from == null ? null : cb.greaterThanOrEqualTo(root.get("eventDateTime"), from);
    }

    public static Specification<Event> eventDateBefore(LocalDateTime to) {
        return (root, query, cb) ->
            to == null ? null : cb.lessThanOrEqualTo(root.get("eventDateTime"), to);
    }
}
```

### Jackson Configuration for snake_case JSON

By default, Spring Boot's Jackson serializes Java camelCase fields as camelCase JSON. To produce `event_date_time` instead of `eventDateTime`, add to `application.yml`:

```yaml
# [ASSUMED] Jackson naming strategy in Spring Boot
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
```

Alternatively, annotate individual DTOs with `@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)`. Claude's discretion applies here — either approach is valid. Using the global `application.yml` setting is simpler and consistent across all DTOs.

---

## State of the Art

| Old Approach | Current Approach | Impact |
|--------------|------------------|--------|
| `@RepositoryRestResource` auto-CRUD | Manual @RestController per resource | Explicit control over response shape and business rules |
| Spring `Criteria API` directly in service | `JpaSpecificationExecutor` + `Specification<T>` static builders | Testable, composable predicates |
| Manual null-check loops for PATCH | MapStruct `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)` | Compile-time generation, no runtime overhead |
| `Page<T>` returned directly from controller | Custom `PagedResponse<T>` wrapper | Stable API contract independent of Spring Page internals |

**Deprecated / not applicable:**
- `@EnableSpringDataWebSupport`: Not needed in Boot 3.x — `Pageable` auto-resolution is enabled by default via `spring-boot-autoconfigure`.
- `CrudRepository.findAll(Example)` (Query by Example): Works for simple equality filters but cannot handle range filters (dateFrom/dateTo). Use `JpaSpecificationExecutor` instead.

---

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | MapStruct `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` with `@MappingTarget` generates correct partial-update code for this version (1.6.3) | Pattern 3 | PATCH updates all fields including nulls — data loss on update |
| A2 | `ResponseStatusException(HttpStatus.NOT_FOUND, message)` translates to HTTP 404 without any additional `@ExceptionHandler` configuration | Pattern 4 | 404s become 500s until Phase 3 error handler is added |
| A3 | Spring Data JPA `Specification` returns `null` predicate (not a NullPointerException) when the specification method returns `null` | Pattern 2 | Filter is incorrectly applied when parameter is absent |
| A4 | `@PageableDefault` annotation on controller parameter sets the default page size without any additional configuration | Pattern 6 | All paginated endpoints return wrong default page size |
| A5 | `JpaSpecificationExecutor.findAll(Specification, Pageable)` works correctly with `@SpringBootTest` + Testcontainers (same test infrastructure as Phase 1) | Test approach | Integration tests cannot verify filtering behavior |

---

## Open Questions

1. **EVNT-01: "ticket count and price" in event creation**
   - What we know: The Event entity has no `totalTickets` or base `price` field. Tickets are separate entities with individual prices.
   - What's unclear: Does EVNT-01 mean the POST /api/events body should accept ticket creation parameters directly, or does the organizer create tickets separately via POST /api/events/{id}/tickets?
   - Recommendation: Per CONTEXT.md D-05 and D-09, tickets are managed separately. The Phase 2 CREATE event endpoint does NOT accept ticket count or price. EVNT-01's "ticket count and price" is satisfied by the ticket CRUD sub-resource. If this interpretation is wrong, it's a scope issue — not a technical one.

2. **`ResponseStatusException` for Phase 2 vs. deferring to Phase 3**
   - What we know: CONTEXT.md says "No input validation or error handling (those are Phase 3)". ROADMAP.md success criterion 4 requires "subsequent GET for that ID returns 404".
   - What's unclear: Is `ResponseStatusException` considered "error handling" that belongs in Phase 3?
   - Recommendation: Use `ResponseStatusException(NOT_FOUND)` inline in service methods. It is not "error handling infrastructure" — it is a single-line HTTP status signal. Phase 3 will add the formatted JSON error response body on top of it.

---

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java 21 | All code | Yes (Phase 1 compiled) | 21 | — |
| Maven | Build | Yes (Phase 1 built) | Inherited from Phase 1 | — |
| PostgreSQL | Integration tests (Testcontainers) | Yes (Docker) | postgres:17-alpine via Testcontainers | — |
| Docker | Testcontainers | Yes (Phase 1 IT passed) | Inherited from Phase 1 | — |

No missing dependencies. All required tooling was confirmed working in Phase 1.

---

## Security Domain

`security_enforcement: true`, `security_asvs_level: 1` per `.planning/config.json`.

### Applicable ASVS Categories (Level 1)

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | No auth in Phase 2 (AUTH-01 is a Future requirement) |
| V3 Session Management | No | No sessions in Phase 2 |
| V4 Access Control | Partial | Business-rule guards (DRAFT-only CUD for tickets, DRAFT-only delete for events) enforced in service layer — not user auth-based |
| V5 Input Validation | Deferred to Phase 3 | QUAL-01 scoped to Phase 3; Bean Validation not added in Phase 2 |
| V6 Cryptography | No | No cryptographic operations in Phase 2 |

### Known Threat Patterns for Spring MVC + JPA

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL Injection via filter params | Tampering | JPA Specification uses parameterized queries (PreparedStatement) — safe by construction. Never concatenate user input into JPQL strings. |
| UUID enumeration via sequential IDs | Information Disclosure | UUIDs are already used as PKs (Phase 1 decision). No sequential exposure. |
| Unintended PATCH of status field | Tampering | D-16 explicitly prohibits `status` in UpdateEventRequest. MapStruct mapper marks it `@Mapping(target="status", ignore=true)`. |
| Mass assignment (exposing internal fields) | Tampering | Separate request DTOs (CreateEventRequest, UpdateEventRequest) prevent direct entity binding. Entity fields not in the request DTO cannot be set. |

**Security note for Phase 3:** `spring-boot-starter-validation` (Bean Validation) must be added in Phase 3 to enforce required fields and value bounds. Without it, null fields can reach the service layer and cause DB constraint violations with unhelpful error messages.

---

## Sources

### Primary (codebase — directly read)
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/entity/` — All entities read; EventStatus confirmed to have 4 values (DRAFT absent); `@ManyToMany performers` is lazy
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/` — All DTOs are Java records; response shape confirmed
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/` — All mappers read; none have `@MappingTarget` update methods yet (must add)
- `event-service/src/main/resources/db/migration/V3__create_events_table.sql` — CONFIRMED: no CHECK constraint on `status` column; default is `'UPCOMING'`
- `event-service/pom.xml` — CONFIRMED: no new dependencies needed for Phase 2; all required libraries present
- `.planning/config.json` — nyquist_validation: false; security_enforcement: true; security_asvs_level: 1

### Secondary (project documentation)
- `.planning/phases/02-event-crud-business-logic/02-CONTEXT.md` — All 16 locked decisions
- `.planning/REQUIREMENTS.md` — EVNT-01 through EVNT-10 definitions
- `.planning/ROADMAP.md` — Phase 2 success criteria (including `/upcoming` endpoint)
- `.claude/CLAUDE.md` — Tech stack constraints (Spring MVC NOT WebFlux; springdoc 2.x NOT 3.x)

### Tertiary (training knowledge — tagged ASSUMED)
- Spring Data JPA Specification API patterns [ASSUMED]
- MapStruct `@BeanMapping` partial update pattern [ASSUMED]
- Spring MVC `@PageableDefault` and Pageable auto-resolution [ASSUMED]
- `ResponseStatusException` HTTP translation behavior [ASSUMED]

---

## Metadata

**Confidence breakdown:**
- Standard stack (no new deps): HIGH — verified by reading pom.xml
- Flyway V6 migration scope: HIGH — verified by reading V3 migration (no CHECK constraint)
- Architecture patterns (service/controller/specification): MEDIUM — standard Spring Boot patterns from training, not Context7-verified this session
- Pitfalls (N+1, LazyInit): MEDIUM — well-known Hibernate patterns from training

**Research date:** 2026-07-10
**Valid until:** 2026-08-10 (stable Spring Boot patterns; Boot 3.3.x is not fast-moving)
