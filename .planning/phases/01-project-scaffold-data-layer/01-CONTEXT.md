# Phase 1: Project Scaffold & Data Layer - Context

**Gathered:** 2026-06-19
**Status:** Ready for planning

<domain>
## Phase Boundary

Establish the project foundation: a compilable multi-module Maven project with PostgreSQL database layer — Flyway migrations, JPA entities with UUID primary keys, repositories, and MapStruct DTO mapping. No REST endpoints or business logic (those are Phase 2).

</domain>

<decisions>
## Implementation Decisions

### Event Category Design
- **D-01:** Category field uses a Java enum (not free-text String) for type safety and consistent filtering
- **D-02:** Six predefined categories: CONCERT, SPORTS, THEATER, CONFERENCE, FESTIVAL, EXHIBITION

### Entity Audit Fields
- **D-03:** Event entity includes `created_at` and `updated_at` timestamp columns
- **D-04:** Timestamps auto-maintained via JPA lifecycle callbacks (`@PrePersist` / `@PreUpdate`), not Spring Data Auditing

### Claude's Discretion
- Category enum implementation details (DB storage strategy, enum naming) — Claude chose Java enum based on type safety and filtering consistency
- Event status enum: UPCOMING, ONGOING, COMPLETED, CANCELLED (defined in ROADMAP.md success criteria)
- Module structure: parent + event-service (2 modules, per ROADMAP.md scope)
- All other entity field types (BigDecimal for price, LocalDateTime for date/time, Integer for ticket count, etc.)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Context
- `.planning/PROJECT.md` — Key decisions: UUID PKs, Lombok, multi-module Maven, port 8081, MapStruct + Lombok annotation processor ordering
- `.planning/REQUIREMENTS.md` — Phase 1 requirements: INFR-01, INFR-02, INFR-04
- `.planning/ROADMAP.md` — Phase 1 success criteria and entity field list

### Technology Stack
- `.claude/CLAUDE.md` §Technology Stack — Complete version matrix and "What NOT to Use" constraints

No external specs — requirements fully captured in decisions above and referenced documents.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- None — greenfield project, no code exists yet

### Established Patterns
- None — patterns will be established by this phase

### Integration Points
- None — this is the foundational phase

</code_context>

<specifics>
## Specific Ideas

No specific requirements — open to standard approaches

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 1-Project Scaffold & Data Layer*
*Context gathered: 2026-06-19*
