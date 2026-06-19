# Requirements: Ticket Booking Backend

**Defined:** 2026-06-19
**Core Value:** A working end-to-end event booking flow — from creating events to consumers successfully purchasing tickets

## v1 Requirements

### Event Management

- [ ] **EVNT-01**: Organizer can create an event with name, description, category, venue, date/time, ticket count, and price
- [ ] **EVNT-02**: Anyone can view a list of all events
- [ ] **EVNT-03**: Anyone can view a single event by ID
- [ ] **EVNT-04**: Organizer can update an existing event (partial update)
- [ ] **EVNT-05**: Organizer can delete an event
- [ ] **EVNT-06**: Anyone can filter events by category
- [ ] **EVNT-07**: Anyone can filter events by city
- [ ] **EVNT-08**: Anyone can view upcoming events (sorted by date)
- [ ] **EVNT-09**: Event has status management (UPCOMING, ONGOING, COMPLETED, CANCELLED)
- [ ] **EVNT-10**: Event listings support pagination

### Input Validation & Error Handling

- [ ] **QUAL-01**: API validates request bodies with Bean Validation and returns clear error messages
- [ ] **QUAL-02**: API returns consistent error response format (status, message, timestamp, field errors)
- [ ] **QUAL-03**: Non-existent resource returns 404 with descriptive message

### Infrastructure

- [ ] **INFR-01**: Multi-module Maven project structure with parent POM and event-service module
- [ ] **INFR-02**: Database schema managed by Flyway migrations (not ddl-auto)
- [ ] **INFR-03**: API documentation available via Swagger UI (springdoc-openapi)
- [ ] **INFR-04**: DTO mapping via MapStruct (entities never exposed in API responses)

## Future Requirements

### Ticket & Booking (ticket-service)

- **TICK-01**: User can purchase tickets for an event
- **TICK-02**: Concurrent booking handled by Redis distributed lock (Redisson)
- **TICK-03**: Ticket reservation with TTL auto-expiry via Redis
- **TICK-04**: User can view order history
- **TICK-05**: Booking endpoint is idempotent (prevents duplicate orders)
- **TICK-06**: Payment handled within ticket-service

### Authentication & Authorization

- **AUTH-01**: User can register and login (JWT)
- **AUTH-02**: Role-based access (consumer vs organizer)

### Microservices Infrastructure

- **MSVC-01**: Service Discovery via Eureka
- **MSVC-02**: API Gateway for routing and centralized auth
- **MSVC-03**: Inter-service communication via OpenFeign

## Out of Scope

| Feature | Reason |
|---------|--------|
| Frontend UI | Backend API only project |
| Real payment gateway integration | Mock payment — real integration adds liability, not learning |
| Seat map / seat selection | Frontend concern, extreme complexity |
| Social features | Out of domain |
| Dynamic pricing | Business logic complexity with minimal architectural learning |
| Multi-currency support | Adds noise, not value for learning |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| EVNT-01 | Phase 2 | Pending |
| EVNT-02 | Phase 2 | Pending |
| EVNT-03 | Phase 2 | Pending |
| EVNT-04 | Phase 2 | Pending |
| EVNT-05 | Phase 2 | Pending |
| EVNT-06 | Phase 2 | Pending |
| EVNT-07 | Phase 2 | Pending |
| EVNT-08 | Phase 2 | Pending |
| EVNT-09 | Phase 2 | Pending |
| EVNT-10 | Phase 2 | Pending |
| QUAL-01 | Phase 3 | Pending |
| QUAL-02 | Phase 3 | Pending |
| QUAL-03 | Phase 3 | Pending |
| INFR-01 | Phase 1 | Pending |
| INFR-02 | Phase 1 | Pending |
| INFR-03 | Phase 3 | Pending |
| INFR-04 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0

---
*Requirements defined: 2026-06-19*
*Last updated: 2026-06-19 after roadmap creation*
