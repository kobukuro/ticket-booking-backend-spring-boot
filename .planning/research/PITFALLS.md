# Pitfalls Research: Ticket Booking Backend

**Researched:** 2026-06-19
**Confidence:** HIGH

## Critical Pitfalls

### 1. Double Booking (Race Condition)
**Risk:** Two users book the last ticket simultaneously — both succeed, overselling occurs.
**Warning signs:** `availableTickets` goes negative; more orders than total tickets.
**Prevention:** Redis distributed lock (Redisson) with TTL. Lock on event+seat key, auto-expires if client crashes. Add database constraint `CHECK (available_tickets >= 0)` as safety net.
**Phase:** Ticket Service

### 2. Reservation Timeout Missing
**Risk:** User starts booking but never completes — tickets held forever.
**Warning signs:** Available ticket count doesn't match actual sold tickets.
**Prevention:** Redis key with TTL for reservation holds. When TTL expires, reservation auto-releases. No scheduled cleanup needed.
**Phase:** Ticket Service

### 3. Flyway 10 PostgreSQL Module Missing
**Risk:** Adding only `flyway-core` without `flyway-database-postgresql` causes startup crash.
**Prevention:** Add both `flyway-core` and `flyway-database-postgresql` dependencies.
**Phase:** Event Service (scaffolding)

### 4. MapStruct + Lombok Annotation Processor Order
**Risk:** If MapStruct runs before Lombok, generated mappers reference nonexistent getters/setters.
**Prevention:** Strict order in Maven compiler plugin: Lombok → lombok-mapstruct-binding → MapStruct processor.
**Phase:** Event Service (scaffolding)

### 5. Missing Idempotency on Booking Endpoint
**Risk:** Network retry or user double-click creates duplicate bookings.
**Prevention:** Idempotency key pattern or unique constraint on (user_id, event_id, reservation_window).
**Phase:** Ticket Service

## Moderate Pitfalls

### 6. UUID v4 Primary Key Performance
**Risk:** Random UUIDs cause B-tree index fragmentation in PostgreSQL.
**Prevention:** Use UUID v7 (time-ordered) for better insert performance with same uniqueness.
**Phase:** Event Service (entity design)

### 7. Lombok `@Data` on JPA Entities
**Risk:** `@Data` generates equals/hashCode using all fields, causing `LazyInitializationException`.
**Prevention:** Use `@Getter @Setter` on entities. Write manual equals/hashCode based on ID only.
**Phase:** Event Service (entity design)

### 8. springdoc-openapi Version Confusion
**Risk:** springdoc v3.0.x targets Spring Boot 4, incompatible with Boot 3.3.x.
**Prevention:** Use springdoc-openapi 2.x line (2.8.17) for Spring Boot 3.x.
**Phase:** Event Service (API docs)

### 9. Missing Validation Starter
**Risk:** `@Valid`, `@NotNull` annotations silently do nothing without the starter.
**Prevention:** Add `spring-boot-starter-validation` explicitly.
**Phase:** Event Service

### 10. Missing `open-in-view: false`
**Risk:** Spring Boot defaults to `true`, hiding lazy loading issues.
**Prevention:** Set `spring.jpa.open-in-view: false` in application.yml.
**Phase:** Event Service (configuration)

## Minor Pitfalls

### 11. Exposing JPA Entities in API Responses
**Risk:** Internal DB structure leaks to API consumers.
**Prevention:** Always use DTOs. MapStruct for entity-DTO conversion.
**Phase:** All services

### 12. HikariCP Pool Exhaustion
**Risk:** Default pool size (10) too small during integration tests.
**Prevention:** Configure pool size per environment.
**Phase:** Testing setup

## Phase-Specific Summary

| Phase | Watch For |
|-------|-----------|
| Event Service scaffolding | Flyway 10 module, MapStruct+Lombok order, validation starter |
| Event Service entities | Lombok @Data on entities, UUID strategy, open-in-view |
| Ticket Service | Double booking (Redis lock), reservation timeout, idempotency |
| API Gateway / Auth | Spring Security 6 API changes |

---
*Researched: 2026-06-19*
