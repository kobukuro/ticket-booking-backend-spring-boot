# Phase 1: Project Scaffold & Data Layer - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-06-19
**Phase:** 1-Project Scaffold & Data Layer
**Areas discussed:** Event 分類方式, Entity 審計欄位

---

## Event 分類方式

### Q1: Event 的 category 欄位要怎麼設計？

| Option | Description | Selected |
|--------|-------------|----------|
| Java enum（推薦） | 預定義固定分類（如 CONCERT, SPORTS, THEATER, CONFERENCE）。篩選時型別安全，新增分類需改程式。 | |
| 自由文字 String | 用戶可以填任意分類名稱，更靈活但沒有統一標準，篩選時可能有大小寫、拼寫不一致的問題。 | |
| Claude 決定 | 讓 Claude 根據最佳實踐選擇。 | ✓ |

**User's choice:** Claude 決定
**Notes:** Claude chose Java enum for type safety and filtering consistency.

### Q2: 需要哪些活動分類？

| Option | Description | Selected |
|--------|-------------|----------|
| 4 類基本款 | CONCERT、SPORTS、THEATER、CONFERENCE — 足以涵蓋 PROJECT.md 列的場景。 | |
| 6 類擴充款 | 基本 4 類 + FESTIVAL、EXHIBITION — 讓篩選更豐富。 | ✓ |
| Claude 決定 | 讓 Claude 根據專案場景選擇合適的分類數量。 | |

**User's choice:** 6 類擴充款
**Notes:** Final categories: CONCERT, SPORTS, THEATER, CONFERENCE, FESTIVAL, EXHIBITION

---

## Entity 審計欄位

### Q1: Event entity 要加哪些審計欄位？

| Option | Description | Selected |
|--------|-------------|----------|
| created_at + updated_at（推薦） | 加建立和更新時間戳，用 JPA @PrePersist / @PreUpdate 自動維護。標準實踐，成本低。 | |
| 完整審計 | created_at + updated_at + created_by + updated_by。但目前沒有認證系統，created_by 會是空值。 | |
| 先不加 | 保持最小 schema，等有需求再透過 Flyway migration 新增。 | |

**User's choice:** Other — 詢問 "要用 last_updated_at 嗎"
**Notes:** User asked about naming convention (updated_at vs last_updated_at). Claude explained that `updated_at` is the industry standard in Spring/JPA ecosystem, pairing symmetrically with `created_at`. User confirmed standard naming.

### Q2: 用 created_at + updated_at 這組標準命名，可以嗎？

| Option | Description | Selected |
|--------|-------------|----------|
| 好，用標準命名 | created_at + updated_at，用 JPA @PrePersist / @PreUpdate 自動維護 | ✓ |
| 用 last_updated_at | 偏好更明確的命名風格 | |

**User's choice:** 好，用標準命名

---

## Claude's Discretion

- Category field implementation: Claude chose Java enum over free-text String
- Event status enum values (from ROADMAP.md): UPCOMING, ONGOING, COMPLETED, CANCELLED
- Module structure (not discussed): parent + event-service
- Entity field types: BigDecimal for price, LocalDateTime for date/time, etc.

## Deferred Ideas

None — discussion stayed within phase scope.
