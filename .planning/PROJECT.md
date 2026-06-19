# Ticket Booking Backend

## What This Is

A ticket booking backend system built with Spring Boot microservices architecture. Consumers can browse events and purchase tickets, while organizers can create and manage events. Built as a learning project and portfolio piece to demonstrate microservices patterns.

## Core Value

A working end-to-end event booking flow — from creating events to consumers successfully purchasing tickets.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Event CRUD (create, read, update, delete events)
- [ ] Filter events by category, city, and status
- [ ] Track available ticket count per event
- [ ] Multi-module Maven project structure for future microservice expansion

### Future

- User authentication / JWT — add with api-gateway
- Ticket service — handle ticket purchasing, order records, inventory deduction, payment
- API Gateway for routing and auth
- Service Discovery (Eureka) — add when multiple services exist

### Out of Scope

- Frontend UI — this is a backend API only project

## Context

- Target scenarios: concerts, sports events, theater shows, conferences
- First microservice is event-service; ticket-service, api-gateway, etc. will follow
- Tech stack decisions finalized during initial discussion

## Constraints

- **Tech stack**: Java 21, Spring Boot 3.3.x, Maven, PostgreSQL, JPA, Lombok, Redis
- **Port convention**: event-service on 8081, reserve 8080 for future API Gateway

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| UUID as Entity PK | Avoids ID collision across microservices | — Pending |
| Use Lombok | Reduce boilerplate for entities and DTOs | — Pending |
| Multi-module Maven structure | Easy to add new microservice modules later | — Pending |
| Port 8081 for event-service | Reserve 8080 for API Gateway | — Pending |
| Redis distributed lock (Redisson) for ticket booking | Best practice for concurrency control, auto-expiry via TTL | — Pending |
| Payment inside ticket-service | Avoids distributed transaction complexity, split later if needed | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-06-19 after initialization*
