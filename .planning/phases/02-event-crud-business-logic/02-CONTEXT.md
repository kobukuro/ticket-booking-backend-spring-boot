# Phase 2: Event CRUD & Business Logic - Context

**Gathered:** 2026-07-10
**Status:** Ready for planning

<domain>
## Phase Boundary

Build all REST API endpoints for event management on port 8081: full CRUD for Event, Venue, Performer, and Ticket resources, with filtering, pagination, and status management. No input validation or error handling (those are Phase 3).

</domain>

<decisions>
## Implementation Decisions

### EventStatus — DRAFT added
- **D-01:** EventStatus enum gains a new DRAFT value (in addition to UPCOMING, ONGOING, COMPLETED, CANCELLED)
- **D-02:** Status transition rules:
  - DRAFT ↔ UPCOMING (reversible — publish and unpublish)
  - UPCOMING → ONGOING or CANCELLED
  - ONGOING → COMPLETED or CANCELLED
  - COMPLETED and CANCELLED are terminal (no further transitions)
- **D-03:** Status transitions exposed as dedicated action endpoints (`POST /api/events/{id}/publish`, `/unpublish`, `/cancel`, `/start`, `/complete`) — NOT via PATCH status field, to enforce the state machine

### CRUD Scope
- **D-04:** Full CRUD for Event, Venue, Performer
- **D-05:** Full CRUD for Ticket, but create/update/delete are restricted to events in DRAFT status only. Read (GET) is always allowed.
- **D-06:** Event delete is only allowed when event is in DRAFT status

### API Resource Structure
- **D-07:** Create event (`POST /api/events`) accepts `venueId` (UUID) and `performerIds` (UUID array) in request body
- **D-08:** Event response includes full venue details and full performer details (not just IDs)
- **D-09:** Tickets are managed via separate endpoints (`/api/events/{id}/tickets` and `/api/tickets/{id}`)

### Filtering & Pagination
- **D-10:** Event list supports filters: `category`, `city`, `status`, `dateFrom`, `dateTo` as query parameters
- **D-11:** Pagination: offset-based (`?page=0&size=20`) using Spring Data `Pageable`
- **D-12:** Default sort: `event_date_time` ascending (nearest event first)

### Update Behavior
- **D-13:** Event update uses `PATCH` (partial update) — only provided fields are updated
- **D-14:** Performer membership managed via dedicated sub-resource endpoints:
  - `POST /api/events/{id}/performers/{performerId}` — add performer to event
  - `DELETE /api/events/{id}/performers/{performerId}` — remove performer from event
- **D-15:** Venue update in PATCH body via `venueId` field (single value, not a collection)
- **D-16:** PATCH on Event does NOT accept `status` field — use action endpoints instead

### Claude's Discretion
- Default page size (recommend 20)
- Exact request/response field names (follow Java naming conventions → snake_case in JSON via Jackson)
- HTTP status codes for each endpoint (standard REST conventions)
- Whether to support `GET /api/events/upcoming` as a shortcut or just use `?status=UPCOMING&sort=eventDateTime`

</decisions>

<api_design>
## API Endpoints

### Event
| Method | Path | Description | Restriction |
|--------|------|-------------|-------------|
| POST | /api/events | Create event | — |
| GET | /api/events | List events (filter + paginate) | — |
| GET | /api/events/{id} | Get event by ID | — |
| PATCH | /api/events/{id} | Partial update event | — |
| DELETE | /api/events/{id} | Delete event | DRAFT only |
| POST | /api/events/{id}/publish | DRAFT → UPCOMING | DRAFT only |
| POST | /api/events/{id}/unpublish | UPCOMING → DRAFT | UPCOMING only |
| POST | /api/events/{id}/start | UPCOMING → ONGOING | UPCOMING only |
| POST | /api/events/{id}/cancel | → CANCELLED | UPCOMING or ONGOING |
| POST | /api/events/{id}/complete | ONGOING → COMPLETED | ONGOING only |

### Event ↔ Performer sub-resource
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/events/{id}/performers/{performerId} | Add performer to event |
| DELETE | /api/events/{id}/performers/{performerId} | Remove performer from event |

### Venue
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/venues | Create venue |
| GET | /api/venues | List venues |
| GET | /api/venues/{id} | Get venue by ID |
| PATCH | /api/venues/{id} | Partial update venue |
| DELETE | /api/venues/{id} | Delete venue |

### Performer
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/performers | Create performer |
| GET | /api/performers | List performers |
| GET | /api/performers/{id} | Get performer by ID |
| PATCH | /api/performers/{id} | Partial update performer |
| DELETE | /api/performers/{id} | Delete performer |

### Ticket
| Method | Path | Description | Restriction |
|--------|------|-------------|-------------|
| POST | /api/events/{id}/tickets | Create ticket for event | Event DRAFT only |
| GET | /api/events/{id}/tickets | List tickets for event | — |
| GET | /api/tickets/{id} | Get ticket by ID | — |
| PATCH | /api/tickets/{id} | Partial update ticket | Event DRAFT only |
| DELETE | /api/tickets/{id} | Delete ticket | Event DRAFT only |

</api_design>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Context
- `.planning/PROJECT.md` — Core value, constraints, key decisions
- `.planning/REQUIREMENTS.md` — Phase 2 requirements: EVNT-01 through EVNT-10
- `.planning/ROADMAP.md` — Phase 2 goal and success criteria
- `.planning/phases/01-project-scaffold-data-layer/01-CONTEXT.md` — Phase 1 decisions (entity design, UUID PKs, JPA patterns)

### Technology Stack
- `.claude/CLAUDE.md` §Technology Stack — Complete version matrix and "What NOT to Use" constraints
  - springdoc-openapi 2.8.17 (NOT 3.x)
  - Spring MVC (NOT WebFlux)
  - Bean Validation via `spring-boot-starter-validation`

### Existing Code
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/entity/` — All JPA entities (read before creating services)
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/dto/` — Existing DTOs (extend, don't replace)
- `event-service/src/main/java/com/kobukuro/ticketbooking/event/mapper/` — Existing MapStruct mappers
- `event-service/src/main/resources/db/migration/` — V1–V5 Flyway migrations (new migrations must be V6+)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- All entities already built: Event, Venue, Performer, Ticket (with enums EventCategory, EventStatus, TicketStatus)
- Repositories already exist: EventRepository, VenueRepository, PerformerRepository, TicketRepository
- DTOs already exist: EventDto, VenueDto, PerformerDto, TicketDto (may need new request DTOs)
- MapStruct mappers already exist for all entities

### Patterns to Follow
- Package structure: `com.kobukuro.ticketbooking.event.<layer>` (entity, repository, dto, mapper, service, controller)
- All entities use UUID primary keys with `@GeneratedValue(strategy = GenerationType.UUID)`
- Audit timestamps via `@PrePersist`/`@PreUpdate` (not Spring Data Auditing)
- Enums stored as `@Enumerated(EnumType.STRING)`

### Integration Points
- EventStatus enum needs DRAFT added (update entity + add Flyway migration if CHECK constraint exists)
- TicketDto already has `eventId` field mapped from `event.id`
- Event entity has `@ManyToMany performers` via `event_performers` join table

### What Needs to Be Built
- Request DTOs (separate from response DTOs): CreateEventRequest, UpdateEventRequest, CreateVenueRequest, etc.
- Service layer: EventService, VenueService, PerformerService, TicketService
- Controller layer: EventController, VenueController, PerformerController, TicketController
- Custom query in EventRepository for filtering (Spring Data Specification or custom JPQL)

</code_context>

<specifics>
## Specific Implementation Notes

- **DRAFT in migration**: Check V3 migration for any CHECK constraint on `status` column — if so, add V6 migration to add DRAFT to the constraint
- **Filtering**: Use Spring Data JPA Specification (or `@Query`) for dynamic filter composition — avoids multiple repository methods
- **Pagination response**: Return Spring's `Page<T>` mapped to a custom response wrapper with `content`, `totalPages`, `totalElements`, `number`, `size`
- **Performer sub-resource**: These endpoints only modify the join table (`event_performers`), no new entity needed

</specifics>

<deferred>
## Deferred Ideas

- `GET /api/events/upcoming` shortcut endpoint — deferred to Claude's discretion during planning
- date range filter UI/UX details — Claude decides query param naming (dateFrom/dateTo vs startDate/endDate)
- Online events (virtual venue) — deferred to future phase

</deferred>

---

*Phase: 2 — Event CRUD & Business Logic*
*Context gathered: 2026-07-10*
