# Features Research: Ticket Booking Backend

**Researched:** 2026-06-19
**Confidence:** HIGH

## Table Stakes — Consumer-Facing

| Feature | Complexity | Dependencies |
|---------|-----------|-------------|
| Event browsing with filters (category, city, date) | Low | Event Service |
| Event detail page (info, pricing, availability) | Low | Event Service |
| Ticket purchasing with inventory tracking | Medium | Event + Ticket Service |
| Order confirmation | Low | Ticket Service |
| Order history | Low | Ticket Service + Auth |
| User registration and login | Medium | Auth Service |
| Search events by keyword | Medium | Event Service |
| Pagination on event listings | Low | Event Service |

## Table Stakes — Organizer-Facing

| Feature | Complexity | Dependencies |
|---------|-----------|-------------|
| Event CRUD (create, read, update, delete) | Low | Event Service |
| Ticket type and pricing configuration | Medium | Event + Ticket Service |
| Sales visibility (how many tickets sold) | Low | Ticket Service |
| Event status management (draft/published/cancelled) | Low | Event Service |

## Differentiators (Portfolio Value)

| Feature | Complexity | Why It Impresses |
|---------|-----------|-----------------|
| Async event processing (messaging) | High | Demonstrates event-driven architecture |
| Multiple ticket tiers (VIP, regular, early bird) | Medium | Real-world pricing model |
| Promo codes / discount | Medium | Common e-commerce pattern |
| QR e-ticket generation | Medium | Full lifecycle demonstration |
| Refund handling | Medium | Compensating transaction pattern |
| Seat hold with TTL (reservation timeout) | High | Concurrency + distributed locking |
| Idempotent booking endpoint | Medium | Production-grade API design |
| Circuit breaker on inter-service calls | Medium | Resilience pattern |
| API rate limiting | Medium | Gateway pattern |
| Distributed tracing (Zipkin) | Medium | Observability |
| Health checks and monitoring | Low | DevOps readiness |
| Docker Compose deployment | Medium | Container orchestration |
| Database migrations (Flyway) | Low | Schema evolution |
| API documentation (Swagger UI) | Low | Developer experience |

## Anti-Features (Do NOT Build)

| Feature | Reason |
|---------|--------|
| Frontend UI | Backend-only project per PROJECT.md |
| Real payment processing | Mock it — real payment adds liability, not learning |
| Seat map rendering | Frontend concern, extreme complexity |
| Social features (sharing, reviews) | Out of domain |
| Recommendation engine | ML complexity, not relevant to microservices learning |
| Dynamic pricing | Business logic complexity with minimal architectural learning |
| Multi-currency support | Internationalization adds noise, not value |
| Push notifications (mobile) | No mobile client |
| Chat / customer support | Different domain entirely |
| Multi-tenancy | Over-engineering for a portfolio project |

## Feature Dependency Graph

```
Event CRUD (foundation)
  └── Event Filtering / Pagination
  └── Event Status Management
  └── Ticket Type Configuration
        └── Ticket Purchasing (requires auth)
              └── Order Confirmation
              └── Order History
              └── Promo Codes
              └── Refund Handling
              └── Seat Hold with TTL (requires Redis)
              └── Payment Processing (requires messaging)
```

## Recommended MVP Phasing

1. **Event Service** — CRUD, filters, pagination, status management
2. **Ticket Service + Auth** — JWT auth, purchasing with concurrency control, orders
3. **Infrastructure** — API Gateway, Eureka, async messaging
4. **Polish + Differentiators** — multi-tier pricing, promo codes, refunds, QR tickets

---
*Researched: 2026-06-19*
