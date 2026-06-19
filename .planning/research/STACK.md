# Stack Research: Ticket Booking Backend

**Researched:** 2026-06-19
**Confidence:** MEDIUM

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

**Critical:** Annotation processor order in Maven compiler plugin must be: Lombok → lombok-mapstruct-binding → MapStruct processor.

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

Spring Cloud 2023.0.6 is the final OSS release. After July 2025, patches require commercial support. If project lifespan exceeds 12 months, plan migration to Spring Boot 3.4+ / Spring Cloud 2024.0.

---
*Researched: 2026-06-19*
