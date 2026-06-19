# Architecture Research: Ticket Booking Backend

**Researched:** 2026-06-19
**Confidence:** HIGH

## System Topology

```
                    ┌──────────────┐
                    │  API Gateway │ :8080
                    │ (Spring Cloud│
                    │   Gateway)   │
                    └──────┬───────┘
                           │
                    ┌──────┴───────┐
                    │   Eureka     │ :8761
                    │  Discovery   │
                    └──────────────┘
                           │
               ┌───────────┴───────────┐
               │                       │
        ┌──────┴──────┐         ┌──────┴──────┐
        │ Event Svc   │         │ Ticket Svc  │
        │   :8081     │         │   :8082     │
        │ [event_db]  │         │ [ticket_db] │
        └─────────────┘         └─────────────┘
```

Note: Payment is handled within ticket-service (not a separate service). Keeps the booking + payment flow in one transaction boundary, avoids distributed transaction complexity.

## Component Boundaries

| Component | Responsibility | Owns | Communicates With |
|-----------|---------------|------|-------------------|
| event-service | Event CRUD, category management, venue info | event_db | Standalone initially |
| ticket-service | Ticket inventory, booking, orders, payment | ticket_db | Queries event-service |
| api-gateway | Routing, rate limiting, JWT validation | — | Proxies all services |
| discovery-server | Service registry | — | All services register |

## Communication Patterns

### Synchronous (REST via OpenFeign)
- ticket-service → event-service: Check event exists, get pricing, check availability
- api-gateway → all services: Reverse proxy

## Data Flow: Booking

```
1. User → API Gateway → ticket-service: POST /api/v1/bookings
2. ticket-service → event-service: GET /api/v1/events/{id} (verify + get price)
3. ticket-service: Reserve tickets (pessimistic lock), process payment, create order
4. ticket-service → event-service: PUT (decrement available tickets)
```

## Database-Per-Service

Each service owns its database. Separate PostgreSQL databases on same server for dev:
- `event_db` — events, categories
- `ticket_db` — tickets, bookings, orders, payments

## Patterns to Follow

| Pattern | Where | Why |
|---------|-------|-----|
| DTO pattern | All services | Never expose JPA entities in API responses |
| Global exception handler | All services | `@RestControllerAdvice` for consistent error responses |
| Repository pattern | All services | Spring Data JPA with derived queries |
| Database-per-service | All services | Prevents distributed monolith |

## Anti-Patterns to Avoid

| Anti-Pattern | Risk |
|-------------|------|
| Shared database across services | Creates coupling, defeats microservices purpose |
| Synchronous call chains (A → B → C) | Cascading failures |
| Distributed monolith | Services that cannot be deployed independently |
| Premature service extraction | Wasted time on infra before business logic is clear |

## Recommended Build Order

| Order | Service | Rationale |
|-------|---------|-----------|
| 1 | event-service | No dependencies, validates foundation (Maven, JPA, REST) |
| 2 | ticket-service | Core booking flow, inter-service communication |
| 3 | discovery-server (Eureka) | Infrastructure for service registration |
| 4 | api-gateway | Routing, auth, validates Eureka integration |

---
*Researched: 2026-06-19*
