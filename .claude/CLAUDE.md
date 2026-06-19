<!-- GSD:project-start source:PROJECT.md -->

## Project

**Ticket Booking Backend**

A ticket booking backend system built with Spring Boot microservices architecture. Consumers can browse events and purchase tickets, while organizers can create and manage events. Built as a learning project and portfolio piece to demonstrate microservices patterns.

**Core Value:** A working end-to-end event booking flow — from creating events to consumers successfully purchasing tickets.

### Constraints

- **Tech stack**: Java 21, Spring Boot 3.3.x, Maven, PostgreSQL, JPA, Lombok, Redis
- **Port convention**: event-service on 8081, reserve 8080 for future API Gateway

<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->

## Technology Stack

## Core Stack

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Language | Java | 21 (LTS) | Virtual threads, pattern matching, records |
| Framework | Spring Boot | 3.3.13 | Latest stable 3.3.x |
| Build | Maven | Multi-module | Shared dependency management across services |
| Database | PostgreSQL | 16 | ACID, JSON support, mature ecosystem |
| ORM | Spring Data JPA | Managed by Boot | Hibernate 6.x under the hood |
| Migrations | Flyway | 10.x (managed) | Requires `flyway-database-postgresql` module |
| Connection Pool | HikariCP | Managed by Boot | Default, fastest pool |

## API & Documentation

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| REST | Spring MVC | Managed by Boot | Simpler than WebFlux for CRUD-heavy APIs |
| API Docs | springdoc-openapi | 2.8.17 | Do NOT use 3.0.x (targets Boot 4) |
| Validation | Bean Validation | Managed by Boot | `spring-boot-starter-validation` (explicit dep) |

## DTO Mapping

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Mapper | MapStruct | 1.6.3 | Compile-time, type-safe mapping |
| Boilerplate | Lombok | 1.18.34+ | Managed by Boot BOM |
| Bridge | lombok-mapstruct-binding | 0.2.0 | Required for Lombok + MapStruct interop |

## Testing

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Test Framework | JUnit 5 | Managed by Boot | Default |
| Containers | Testcontainers | 2.0.5 | Use `@ServiceConnection` (Boot 3.1+) |
| Mocking | Mockito | Managed by Boot | Default |
| Assertions | AssertJ | Managed by Boot | Fluent assertions |
| API Testing | MockMvc | Managed by Boot | Controller integration tests |

## Microservices Infrastructure (Future)

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Cloud BOM | Spring Cloud | 2023.0.6 (Leyton) | Compatible with Boot 3.3.x |
| Gateway | Spring Cloud Gateway | 4.1.x | Managed by Cloud BOM |
| Discovery | Eureka | 4.1.x | Managed by Cloud BOM |
| Resilience | Resilience4j | Managed by Cloud BOM | Circuit breaker, retry |
| Inter-service | OpenFeign | Managed by Cloud BOM | Declarative REST client |

## Messaging (Future)

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Broker | RabbitMQ | 3.13+ | Task-oriented delivery, simpler than Kafka for booking flows |
| Client | Spring AMQP | Managed by Boot | Native integration |

## Security (Future)

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Auth | Spring Security | 6.x (managed) | Use `SecurityFilterChain` bean, not deprecated adapter |
| JWT | JJWT | 0.12.6 | Most popular JWT library for Java |

## Caching & Locking (Future)

| Component | Recommendation | Version | Rationale |
|-----------|---------------|---------|-----------|
| Cache/Lock | Redis | 7.x | Seat hold TTL, event listing cache |
| Client | Redisson | 4.5.0 | Distributed lock with watchdog renewal |

## What NOT to Use

| Technology | Reason |
|-----------|--------|
| Spring WebFlux | Overkill for CRUD-heavy APIs; adds reactive complexity |
| springdoc-openapi 3.x | Targets Spring Boot 4, incompatible with 3.3.x |
| Kafka | Over-engineered for this scope; RabbitMQ is simpler for task-based messaging |
| CQRS / Event Sourcing | Over-engineering for a learning project |
| GraphQL | REST is simpler and sufficient for this domain |

## Warning: Spring Cloud 2023.0 EOL

<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->

## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->

## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->

## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, `.github/skills/`, or `.codex/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->

## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:

- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->

<!-- GSD:profile-start -->

## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
