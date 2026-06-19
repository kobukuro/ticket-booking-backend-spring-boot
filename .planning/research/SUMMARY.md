# Project Research Summary

**Project:** Ticket Booking Backend (Spring Boot Microservices)
**Domain:** E-commerce / Event Ticketing
**Researched:** 2026-06-19
**Confidence:** HIGH

## Executive Summary

This is a microservices-based event ticket booking backend built with Spring Boot 3.3 and Java 21. The standard approach is to start with a standalone event service (CRUD + REST + JPA), then layer on a ticket service with inter-service communication, and finally add infrastructure (Eureka, API Gateway). This build order follows natural dependency chains: you cannot book tickets without events, and you do not need a gateway until you have multiple services to route to.

The recommended stack is well-established: Spring Boot 3.3.x, PostgreSQL 16, Spring Data JPA, MapStruct for DTO mapping, and Flyway for migrations. For microservices infrastructure, Spring Cloud 2023.0.x provides Gateway, Eureka, OpenFeign, and Resilience4j. Redis with Redisson handles distributed locking for the critical seat-reservation concurrency problem.

The primary risk is double-booking due to race conditions during ticket purchasing. This must be solved with pessimistic locking at the database level (CHECK constraint as safety net) and Redis distributed locks for the reservation flow. Secondary risks are all configuration traps: Flyway 10 requiring an extra PostgreSQL module, MapStruct/Lombok annotation processor ordering, and springdoc-openapi version confusion. These are all preventable with correct initial setup.

## Key Findings

### Recommended Stack

The stack is mature Spring ecosystem throughout. No exotic dependencies.

**Core technologies:**
- **Java 21 LTS**: Virtual threads, records, pattern matching
- **Spring Boot 3.3.13**: Latest stable, manages most dependency versions
- **PostgreSQL 16**: ACID transactions, CHECK constraints for inventory safety
- **MapStruct 1.6.3 + Lombok**: Compile-time DTO mapping with boilerplate reduction
- **Flyway 10.x**: Schema migrations (requires flyway-database-postgresql module)
- **Spring Cloud 2023.0.6**: Gateway, Eureka, OpenFeign, Resilience4j
- **Redis + Redisson**: Distributed locking for seat reservation TTL

**Do not use:** WebFlux, Kafka, CQRS/Event Sourcing, GraphQL, springdoc 3.x.

### Expected Features

**Must have (table stakes):**
- Event CRUD with filtering, pagination, status management
- Ticket purchasing with inventory tracking and concurrency control
- User registration/login with JWT auth
- Order confirmation and history
- Ticket type and pricing configuration

**Should have (differentiators for portfolio):**
- Seat hold with TTL (reservation timeout via Redis)
- Idempotent booking endpoint
- Async event processing (RabbitMQ)
- Multi-tier pricing (VIP, regular, early bird)
- Promo codes and refund handling
- API Gateway with rate limiting
- Distributed tracing, health checks, Docker Compose

**Defer (v2+):**
- Frontend UI, real payment processing, seat maps, recommendations, dynamic pricing

### Architecture Approach

Two core services (event-service, ticket-service) with database-per-service, fronted by Spring Cloud Gateway and Eureka discovery. Payment logic lives inside ticket-service to avoid distributed transactions. Communication is synchronous REST via OpenFeign initially, with RabbitMQ for async flows added later.

**Major components:**
1. **event-service** (:8081) -- Event CRUD, categories, venues; owns event_db
2. **ticket-service** (:8082) -- Booking, orders, payments, inventory; owns ticket_db
3. **api-gateway** (:8080) -- Routing, rate limiting, JWT validation
4. **discovery-server** (:8761) -- Eureka service registry

### Critical Pitfalls

1. **Double booking race condition** -- Redis distributed lock on event+seat key; DB CHECK constraint available_tickets >= 0 as safety net
2. **Reservation timeout missing** -- Redis key with TTL for holds; auto-expires without scheduled cleanup
3. **Flyway 10 PostgreSQL module** -- Must add both flyway-core AND flyway-database-postgresql
4. **MapStruct + Lombok processor order** -- Must be Lombok, then binding, then MapStruct in Maven compiler plugin
5. **Idempotency on booking endpoint** -- Idempotency key or unique constraint on (user_id, event_id, reservation_window)

## Implications for Roadmap

Based on research, suggested phase structure:

### Phase 1: Event Service Foundation
**Rationale:** Zero dependencies, validates the entire scaffold (Maven, JPA, REST, Flyway, MapStruct)
**Delivers:** Working event-service with CRUD, filtering, pagination, status management, Swagger UI
**Addresses:** Event CRUD, filtering, pagination, event status, API documentation
**Avoids:** Flyway module pitfall, MapStruct+Lombok ordering, Lombok @Data on entities, springdoc version confusion, missing validation starter, open-in-view=true

### Phase 2: Ticket Service + Auth
**Rationale:** Core business logic depends on event-service existing; introduces inter-service communication and concurrency
**Delivers:** Ticket purchasing, booking flow, JWT authentication, order management
**Addresses:** Ticket purchasing, order confirmation, order history, user auth, ticket type configuration
**Avoids:** Double booking, reservation timeout, missing idempotency, UUID fragmentation

### Phase 3: Microservices Infrastructure
**Rationale:** Gateway and discovery only make sense once multiple services exist
**Delivers:** Eureka discovery, API Gateway with routing and rate limiting, centralized auth
**Addresses:** Service discovery, API rate limiting, centralized JWT validation

### Phase 4: Advanced Features + Polish
**Rationale:** Differentiators layered on stable foundation
**Delivers:** Multi-tier pricing, promo codes, refunds, QR tickets, async messaging, circuit breakers, distributed tracing, Docker Compose
**Addresses:** All remaining differentiator features

### Phase Ordering Rationale

- Event service first because every other component depends on events existing
- Ticket service second because it is the core value proposition and most complex business logic
- Infrastructure third because you need two services before gateway/discovery add value
- Polish last because differentiators are additive, not foundational

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2:** Complex concurrency patterns (Redis distributed locking, pessimistic locking, idempotency keys) -- needs specific implementation research
- **Phase 3:** Spring Cloud Gateway JWT filter configuration, Eureka registration details
- **Phase 4:** QR code generation libraries, RabbitMQ exchange/queue topology for booking events

Phases with standard patterns (skip research-phase):
- **Phase 1:** Standard Spring Boot CRUD service -- extremely well-documented patterns

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM | Versions verified but Spring Cloud 2023.0 is EOL -- may need migration |
| Features | HIGH | Clear domain with well-understood feature set |
| Architecture | HIGH | Standard microservices patterns, no novel components |
| Pitfalls | HIGH | Concrete, actionable prevention strategies for each |

**Overall confidence:** HIGH

### Gaps to Address

- **Spring Cloud EOL:** 2023.0.6 is final OSS release. If project extends past 12 months, plan Boot 3.4+ / Cloud 2024.0 migration
- **Redis deployment:** Research not specific about local dev setup for Redis (Docker vs. embedded for tests)
- **Payment mocking strategy:** Need to define mock payment interface during Phase 2 planning
- **Inter-service data consistency:** Synchronous calls between ticket-service and event-service for decrementing inventory need failure handling strategy beyond circuit breakers

## Sources

Research sources aggregated from individual research files (STACK.md, FEATURES.md, ARCHITECTURE.md, PITFALLS.md). All findings based on Spring Boot and Spring Cloud official documentation patterns and established community practices.

---
*Research completed: 2026-06-19*
*Ready for roadmap: yes*
