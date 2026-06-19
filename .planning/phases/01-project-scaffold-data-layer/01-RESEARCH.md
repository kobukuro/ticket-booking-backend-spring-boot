# Phase 1: Project Scaffold & Data Layer - Research

**Researched:** 2026-06-19
**Domain:** Spring Boot multi-module Maven project setup, JPA/Hibernate data layer, Flyway migrations, MapStruct DTO mapping
**Confidence:** HIGH

## Summary

Phase 1 establishes the project foundation: a multi-module Maven project with a parent POM aggregator and an event-service child module. The event-service uses Spring Boot 3.3.13 with Spring Data JPA, Flyway 10 for schema migrations, PostgreSQL as the database, and MapStruct with Lombok for compile-time DTO mapping.

The core technical challenges are (1) correctly configuring the multi-module Maven structure so the parent manages dependency versions while only the bootable module carries `spring-boot-maven-plugin`, (2) ordering annotation processors correctly (Lombok before MapStruct, with the binding library), and (3) configuring Flyway 10 which requires the separate `flyway-database-postgresql` module. All three are well-documented patterns with clear solutions.

**Primary recommendation:** Build a two-module Maven project (parent POM + event-service) with `spring-boot-starter-parent` as the parent's parent, centralize version properties and annotation processor configuration in the parent POM, and use `ddl-auto=validate` with Flyway managing all schema changes.

<user_constraints>

## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** Category field uses a Java enum (not free-text String) for type safety and consistent filtering
- **D-02:** Six predefined categories: CONCERT, SPORTS, THEATER, CONFERENCE, FESTIVAL, EXHIBITION
- **D-03:** Event entity includes `created_at` and `updated_at` timestamp columns
- **D-04:** Timestamps auto-maintained via JPA lifecycle callbacks (`@PrePersist` / `@PreUpdate`), not Spring Data Auditing

### Claude's Discretion
- Category enum implementation details (DB storage strategy, enum naming) -- Claude chose Java enum based on type safety and filtering consistency
- Event status enum: UPCOMING, ONGOING, COMPLETED, CANCELLED (defined in ROADMAP.md success criteria)
- Module structure: parent + event-service (2 modules, per ROADMAP.md scope)
- All other entity field types (BigDecimal for price, LocalDateTime for date/time, Integer for ticket count, etc.)

### Deferred Ideas (OUT OF SCOPE)
None -- discussion stayed within phase scope

</user_constraints>

<phase_requirements>

## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| INFR-01 | Multi-module Maven project structure with parent POM and event-service module | Multi-module Maven architecture pattern with parent POM aggregator, spring-boot-starter-parent inheritance, Maven wrapper |
| INFR-02 | Database schema managed by Flyway migrations (not ddl-auto) | Flyway 10 setup with flyway-database-postgresql, migration file naming conventions, ddl-auto=validate configuration |
| INFR-04 | DTO mapping via MapStruct (entities never exposed in API responses) | MapStruct 1.6.3 + Lombok integration, annotation processor ordering, componentModel=SPRING, lombok-mapstruct-binding |

</phase_requirements>

## Project Constraints (from CLAUDE.md)

- **Tech stack**: Java 21, Spring Boot 3.3.x, Maven, PostgreSQL, JPA, Lombok, Redis
- **Port convention**: event-service on 8081, reserve 8080 for future API Gateway
- **Spring Boot version**: 3.3.13 (latest stable 3.3.x)
- **MapStruct**: 1.6.3 -- do NOT use versions that target different Spring Boot major versions
- **springdoc-openapi**: Do NOT use 3.0.x (targets Boot 4) -- NOT relevant to this phase but noted
- **Do NOT use**: Spring WebFlux, CQRS/Event Sourcing, GraphQL, Kafka
- **Flyway**: Requires both `flyway-core` AND `flyway-database-postgresql`
- **Annotation processor ordering**: Lombok first, then MapStruct (noted in STATE.md decisions)

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Project structure / build | Build System (Maven) | -- | Maven POM defines module boundaries, dependency versions, build lifecycle |
| Database schema management | Database / Storage | Build System | Flyway migrations run at app startup; migration files are build artifacts |
| Entity definitions (JPA) | API / Backend | Database / Storage | Entities are Java classes that map to DB schema |
| DTO mapping | API / Backend | -- | MapStruct generates mapping code at compile time in the backend tier |
| Repository layer | API / Backend | Database / Storage | Spring Data JPA repositories bridge Java code to database queries |
| Application configuration | API / Backend | -- | application.yml configures datasource, JPA, Flyway, server port |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot Starter Parent | 3.3.13 | Parent POM for dependency/plugin management | Latest stable 3.3.x, manages all Spring dependency versions [CITED: spring.io/blog/2025/06/19/spring-boot-3-3-13-available-now] |
| spring-boot-starter-data-jpa | Managed by Boot | JPA + Hibernate 6 + HikariCP | Standard data access layer for Spring Boot |
| PostgreSQL Driver | Managed by Boot | JDBC driver for PostgreSQL | Runtime dependency for database connectivity |
| Flyway Core | Managed by Boot | Schema migration engine | Spring Boot auto-configures Flyway when on classpath |
| flyway-database-postgresql | Managed by Boot | PostgreSQL-specific Flyway module | Required since Flyway 10 extracted DB-specific code into separate modules [CITED: dev-solve.com/posts/4ae6b9e] |
| MapStruct | 1.6.3 | Compile-time DTO-to-entity mapping | Type-safe, zero-reflection mapping [CITED: mapstruct.org/documentation/stable/reference/html/] |
| mapstruct-processor | 1.6.3 | Annotation processor for MapStruct | Generates mapper implementations at compile time [CITED: mapstruct.org/documentation/stable/reference/html/] |
| Lombok | Managed by Boot BOM | Boilerplate reduction (@Data, @Builder, etc.) | Standard in Spring Boot projects, managed version |
| lombok-mapstruct-binding | 0.2.0 | Bridge between Lombok and MapStruct | Required for Lombok + MapStruct to cooperate [CITED: central.sonatype.com/artifact/org.projectlombok/lombok-mapstruct-binding] |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| spring-boot-starter-web | Managed by Boot | Embedded Tomcat + Spring MVC | Needed for server.port configuration and future REST endpoints |
| spring-boot-starter-test | Managed by Boot | JUnit 5 + Mockito + AssertJ | Integration test for Flyway migration and entity persistence |
| Maven Wrapper | 3.9.x | Reproducible builds without global Maven | Always -- ensures consistent build across environments |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| MapStruct | ModelMapper | ModelMapper is reflection-based (slower, no compile-time safety); MapStruct is locked decision |
| Flyway | Liquibase | Both are mature; Flyway is simpler for SQL-first migrations; locked decision |
| JPA lifecycle callbacks | Spring Data Auditing (@EnableJpaAuditing) | Spring Data Auditing adds dependency; lifecycle callbacks are simpler and locked per D-04 |
| @GeneratedValue(GenerationType.UUID) | @UuidGenerator(style=RANDOM) | Both work in Hibernate 6; GenerationType.UUID is JPA-standard and more portable |

**Installation (Maven dependencies -- not npm):**
All dependencies declared in POM files. No package installation command needed -- Maven resolves dependencies on build.

## Package Legitimacy Audit

> All packages in this phase are from established organizations (Spring/Pivotal, Red Gate/Flyway, MapStruct, Project Lombok) and are well-known in the Java ecosystem. Maven Central is the registry.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| spring-boot-starter-parent | Maven Central | 11+ yrs | Industry standard | github.com/spring-projects/spring-boot | OK | Approved [ASSUMED] |
| spring-boot-starter-data-jpa | Maven Central | 11+ yrs | Industry standard | github.com/spring-projects/spring-boot | OK | Approved [ASSUMED] |
| flyway-core | Maven Central | 12+ yrs | Industry standard | github.com/flyway/flyway | OK | Approved [ASSUMED] |
| flyway-database-postgresql | Maven Central | 2+ yrs (Flyway 10 split) | Ships with Flyway | github.com/flyway/flyway | OK | Approved [ASSUMED] |
| org.mapstruct:mapstruct | Maven Central | 10+ yrs | Widely adopted | github.com/mapstruct/mapstruct | OK | Approved [CITED: mapstruct.org] |
| org.projectlombok:lombok | Maven Central | 14+ yrs | Industry standard | github.com/projectlombok/lombok | OK | Approved [ASSUMED] |
| lombok-mapstruct-binding | Maven Central | 4+ yrs | Standard companion | github.com/projectlombok/lombok | OK | Approved [CITED: central.sonatype.com] |
| postgresql (JDBC driver) | Maven Central | 20+ yrs | Official driver | github.com/pgjdbc/pgjdbc | OK | Approved [ASSUMED] |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

*Note: This is a Java/Maven project. Package legitimacy was assessed based on Maven Central presence, organizational provenance, and ecosystem reputation. The npm-oriented `gsd-tools query package-legitimacy check` command is not applicable to Maven packages.*

## Architecture Patterns

### System Architecture Diagram

```
[Maven Build]
     |
     v
[Parent POM (pom packaging)]
     |
     +---> [event-service module (jar packaging)]
              |
              +---> Spring Boot Application
              |        |
              |        +---> Flyway Auto-Configuration
              |        |        |
              |        |        v
              |        |    [db/migration/*.sql]
              |        |        |
              |        |        v
              |        |    [PostgreSQL: events table]
              |        |
              |        +---> JPA / Hibernate
              |        |        |
              |        |        v
              |        |    [Event Entity <-> events table]
              |        |
              |        +---> Spring Data JPA
              |        |        |
              |        |        v
              |        |    [EventRepository interface]
              |        |
              |        +---> MapStruct (compile-time generated)
              |                 |
              |                 v
              |             [EventMapper: Event <-> EventDto]
              |
              +---> [application.yml]
                       - server.port: 8081
                       - datasource: PostgreSQL
                       - jpa.hibernate.ddl-auto: validate
                       - flyway.enabled: true
```

### Recommended Project Structure

```
ticket-booking-backend/              # Project root
├── pom.xml                          # Parent POM (packaging: pom)
├── mvnw                             # Maven wrapper script (Unix)
├── mvnw.cmd                         # Maven wrapper script (Windows)
├── .mvn/
│   └── wrapper/
│       └── maven-wrapper.properties # Wrapper config
└── event-service/                   # Child module
    ├── pom.xml                      # Module POM (packaging: jar)
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── com/example/ticketbooking/event/
        │   │       ├── EventServiceApplication.java    # @SpringBootApplication
        │   │       ├── entity/
        │   │       │   ├── Event.java                  # JPA entity
        │   │       │   ├── EventCategory.java          # Enum
        │   │       │   └── EventStatus.java            # Enum
        │   │       ├── dto/
        │   │       │   └── EventDto.java               # Response DTO
        │   │       ├── mapper/
        │   │       │   └── EventMapper.java            # MapStruct mapper interface
        │   │       └── repository/
        │   │           └── EventRepository.java        # Spring Data JPA repository
        │   └── resources/
        │       ├── application.yml                     # App configuration
        │       └── db/
        │           └── migration/
        │               └── V1__create_events_table.sql # Flyway migration
        └── test/
            └── java/
                └── com/example/ticketbooking/event/
                    └── EventServiceApplicationTests.java
```

### Pattern 1: Parent POM with Shared Dependency Management

**What:** Parent POM declares `spring-boot-starter-parent` as its parent and uses `<dependencyManagement>` + `<properties>` to centralize version numbers for all child modules.
**When to use:** Always in multi-module Spring Boot projects.
**Example:**
```xml
<!-- Source: spring.io/guides/gs/multi-module/ -->
<!-- Parent pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.13</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>ticket-booking-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>event-service</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${lombok.version}</version>
                            </path>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok-mapstruct-binding</artifactId>
                                <version>${lombok-mapstruct-binding.version}</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### Pattern 2: Entity with UUID PK and Audit Timestamps

**What:** JPA entity using Hibernate 6 UUID generation with JPA lifecycle callbacks for audit timestamps.
**When to use:** All entities in this project (UUID PK is a locked decision).
**Example:**
```java
// Source: thorben-janssen.com/generate-uuids-primary-keys-hibernate/
// and CONTEXT.md decision D-03/D-04
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ... fields ...

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Pattern 3: MapStruct Mapper with Spring Component Model

**What:** MapStruct mapper interface annotated for Spring DI, generating implementation at compile time.
**When to use:** Every entity-to-DTO mapping.
**Example:**
```java
// Source: mapstruct.org/documentation/stable/reference/html/
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    EventDto toDto(Event event);

    Event toEntity(EventDto dto);
}
```

### Pattern 4: Flyway SQL Migration

**What:** Versioned SQL migration file in standard Flyway naming convention.
**When to use:** Every schema change.
**Example:**
```sql
-- V1__create_events_table.sql
-- Source: Flyway naming convention docs
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    venue VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    event_date_time TIMESTAMP NOT NULL,
    total_tickets INTEGER NOT NULL,
    available_tickets INTEGER NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### Anti-Patterns to Avoid

- **Using `ddl-auto=update` or `ddl-auto=create-drop` with Flyway:** Flyway manages schema. Set `ddl-auto=validate` so Hibernate only verifies entity-table alignment at startup. Using `update` causes Flyway and Hibernate to fight over schema ownership. [CITED: rieckpil.de/howto-best-practices-for-flyway-and-hibernate-with-spring-boot/]
- **Putting `spring-boot-maven-plugin` in the parent POM:** Only the bootable module (event-service) should have this plugin. Library modules must NOT have it or they produce broken uber-JARs. [CITED: spring.io/guides/gs/multi-module/]
- **Wrong annotation processor order:** MapStruct before Lombok causes compile errors because MapStruct cannot find getters/setters that Lombok has not yet generated. Order MUST be: Lombok, MapStruct processor, lombok-mapstruct-binding. [CITED: bootify.io/spring-data/mapstruct-with-maven-and-lombok.html]
- **Omitting `flyway-database-postgresql`:** Since Flyway 10, the core module no longer bundles database-specific code. Without this module, Flyway silently fails to detect PostgreSQL or throws a `FlywayException`. [CITED: dev-solve.com/posts/4ae6b9e]
- **Storing enums as ordinal in the database:** Default JPA enum mapping uses `@Enumerated(EnumType.ORDINAL)` which breaks if enum constants are reordered. Always use `@Enumerated(EnumType.STRING)` for category and status enums. [ASSUMED]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Database schema management | Manual DDL scripts executed ad-hoc | Flyway versioned migrations | Version tracking, rollback support, team coordination, auto-apply on startup |
| Entity-to-DTO mapping | Manual getter/setter copy methods | MapStruct @Mapper interface | Type-safe at compile time, zero runtime reflection, auto-generates for matching field names |
| UUID generation | Manual `UUID.randomUUID()` in entity constructor | `@GeneratedValue(strategy = GenerationType.UUID)` | JPA manages lifecycle, Hibernate 6 handles it natively, consistent across persistence operations |
| Getter/setter/constructor boilerplate | Manual Java boilerplate | Lombok @Data/@Getter/@Setter/@Builder | Reduces entity class from 100+ lines to ~30, compile-time generated |
| Connection pooling | Custom DataSource configuration | HikariCP (Spring Boot default) | Auto-configured, fastest Java connection pool, zero setup needed |
| Maven wrapper | Global Maven installation | `mvnw` / `mvnw.cmd` | Reproducible builds, no global Maven install required |

**Key insight:** This phase is almost entirely infrastructure wiring. Every component has a standard Spring Boot integration pattern. The value is in getting the wiring right (processor ordering, Flyway modules, POM structure), not in writing custom logic.

## Common Pitfalls

### Pitfall 1: Annotation Processor Ordering Breaks Compilation
**What goes wrong:** MapStruct generates mapper implementations with compilation errors -- it cannot find getters/setters on Lombok-annotated classes.
**Why it happens:** Maven `annotationProcessorPaths` are processed in declaration order. If MapStruct runs before Lombok, the source classes still have no getters/setters.
**How to avoid:** Declare Lombok FIRST, then mapstruct-processor, then lombok-mapstruct-binding in `<annotationProcessorPaths>`.
**Warning signs:** `java: No property named "X" exists in source parameter` compilation errors.

### Pitfall 2: Missing flyway-database-postgresql Module
**What goes wrong:** Application fails to start with a Flyway-related exception, or Flyway silently does nothing.
**Why it happens:** Flyway 10 extracted database-specific support into separate modules. `flyway-core` alone is insufficient.
**How to avoid:** Add both `flyway-core` and `flyway-database-postgresql` as dependencies. Spring Boot manages both versions when on the classpath.
**Warning signs:** `FlywayException: No database found to handle` or migrations simply not running.

### Pitfall 3: UUID Column Type Mismatch Between Flyway and Hibernate
**What goes wrong:** Application starts but fails with a type mismatch when `ddl-auto=validate` checks the schema.
**Why it happens:** Flyway migration uses `VARCHAR(36)` for UUID while Hibernate expects PostgreSQL native `uuid` type.
**How to avoid:** Use `UUID` as the PostgreSQL column type in migration SQL. Hibernate 6 maps Java `UUID` to PostgreSQL `uuid` natively.
**Warning signs:** `SchemaManagementException` on startup mentioning column type mismatch.

### Pitfall 4: spring-boot-maven-plugin in Wrong POM
**What goes wrong:** Library module fails to build, or produces a broken executable JAR that cannot be used as a dependency.
**Why it happens:** `spring-boot-maven-plugin` repackages the JAR into an executable format that other modules cannot depend on.
**How to avoid:** Only include `spring-boot-maven-plugin` in the POM of the module containing `@SpringBootApplication`.
**Warning signs:** `ClassNotFoundException` when another module tries to use classes from the repackaged JAR.

### Pitfall 5: Enum Ordinal Storage
**What goes wrong:** Adding a new enum constant in the middle of the list silently corrupts existing data mapping.
**Why it happens:** JPA defaults to `@Enumerated(EnumType.ORDINAL)` which stores the integer position, not the name.
**How to avoid:** Always annotate enum fields with `@Enumerated(EnumType.STRING)`. Store as VARCHAR in the migration SQL.
**Warning signs:** Database contains integer values for enum columns instead of readable strings.

### Pitfall 6: Maven Wrapper Not At Root
**What goes wrong:** `./mvnw clean install` from project root fails with "command not found" or runs a different Maven version.
**Why it happens:** Maven wrapper files (`mvnw`, `mvnw.cmd`, `.mvn/`) were generated inside a child module instead of the project root.
**How to avoid:** Generate or place the Maven wrapper at the project root directory, not inside any module.
**Warning signs:** `./mvnw` works from event-service/ but not from the project root.

## Code Examples

### Complete Parent POM

```xml
<!-- Source: spring.io/guides/gs/multi-module/ + bootify.io/spring-data/mapstruct-with-maven-and-lombok.html -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.13</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>ticket-booking-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>Ticket Booking Backend</name>
    <description>Ticket booking backend with Spring Boot microservices</description>

    <modules>
        <module>event-service</module>
    </modules>

    <properties>
        <java.version>21</java.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <configuration>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok</artifactId>
                                <version>${lombok.version}</version>
                            </path>
                            <path>
                                <groupId>org.mapstruct</groupId>
                                <artifactId>mapstruct-processor</artifactId>
                                <version>${mapstruct.version}</version>
                            </path>
                            <path>
                                <groupId>org.projectlombok</groupId>
                                <artifactId>lombok-mapstruct-binding</artifactId>
                                <version>${lombok-mapstruct-binding.version}</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### Complete Event-Service Module POM

```xml
<!-- Source: spring.io/guides/gs/multi-module/ -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.example</groupId>
        <artifactId>ticket-booking-backend</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>event-service</artifactId>
    <name>Event Service</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### application.yml Configuration

```yaml
# Source: Spring Boot docs + Flyway best practices
server:
  port: 8081

spring:
  application:
    name: event-service

  datasource:
    url: jdbc:postgresql://localhost:5432/ticketbooking
    username: ${DB_USERNAME:testuser}
    password: ${DB_PASSWORD:testpass}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

### Complete Event Entity

```java
// Source: thorben-janssen.com + CONTEXT.md decisions D-01 through D-04
@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private EventCategory category;

    @Column(name = "venue", nullable = false)
    private String venue;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "event_date_time", nullable = false)
    private LocalDateTime eventDateTime;

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### EventCategory Enum

```java
// Source: CONTEXT.md decisions D-01, D-02
public enum EventCategory {
    CONCERT,
    SPORTS,
    THEATER,
    CONFERENCE,
    FESTIVAL,
    EXHIBITION
}
```

### EventStatus Enum

```java
// Source: ROADMAP.md success criteria
public enum EventStatus {
    UPCOMING,
    ONGOING,
    COMPLETED,
    CANCELLED
}
```

### EventRepository

```java
// Source: Spring Data JPA standard pattern
public interface EventRepository extends JpaRepository<Event, UUID> {
}
```

### EventMapper

```java
// Source: mapstruct.org/documentation/stable/reference/html/
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    EventDto toDto(Event event);

    Event toEntity(EventDto dto);
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `@GenericGenerator(name="uuid2")` for UUID | `@GeneratedValue(strategy = GenerationType.UUID)` | Hibernate 6 / Jakarta Persistence 3.1 | Simpler, no proprietary annotation needed |
| `flyway-core` bundles all DB support | Separate `flyway-database-postgresql` module | Flyway 10 (2023) | Must add DB-specific module explicitly |
| `@Enumerated(EnumType.ORDINAL)` default | `@Enumerated(EnumType.STRING)` best practice | Long-standing | Prevents data corruption on enum reorder |
| Manual Lombok + MapStruct integration | `lombok-mapstruct-binding` library | 2021 | Eliminates complex workarounds |

**Deprecated/outdated:**
- `@GenericGenerator(strategy = "uuid2")`: Replaced by JPA-standard `GenerationType.UUID` in Hibernate 6
- `spring.jpa.hibernate.ddl-auto=update` with Flyway: Use `validate` instead -- Flyway owns schema changes
- Spring Boot 3.3.x OSS support: Ended June 2025, but 3.3.13 is stable and the locked decision per CLAUDE.md [CITED: spring.io/blog/2025/06/19/spring-boot-3-3-13-available-now]

## Assumptions Log

> List all claims tagged `[ASSUMED]` in this research.

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Package ages and download counts for Spring Boot, Flyway, Lombok, PostgreSQL driver | Package Legitimacy Audit | Low -- these are industry-standard packages with decades of history |
| A2 | `@Enumerated(EnumType.ORDINAL)` is the JPA default requiring explicit STRING override | Anti-Patterns | Medium -- if changed in Jakarta Persistence 3.1, the advice is harmless but unnecessary |
| A3 | PostgreSQL Docker container credentials (testuser/testpass/testdb) suitable for dev | Environment Availability | Low -- container is already running with these creds, can be overridden via env vars |

## Open Questions

1. **Database name for the event-service**
   - What we know: The existing PostgreSQL Docker container uses database `testdb`. The project may want a dedicated database like `ticketbooking`.
   - What's unclear: Whether to create a new database or reuse `testdb`.
   - Recommendation: Use a dedicated database `ticketbooking` (or `ticket_booking`). The plan should include either a Docker Compose file or a manual step to create this database. The application.yml should default to `ticketbooking` with env var overrides.

2. **Base package name**
   - What we know: Standard convention is `com.{company}.{project}` or `com.example.ticketbooking`.
   - What's unclear: The user's preferred group ID and base package.
   - Recommendation: Use `com.example.ticketbooking` as default (standard for learning projects). The event-service subpackage would be `com.example.ticketbooking.event`.

3. **Docker Compose for PostgreSQL**
   - What we know: A PostgreSQL container is already running (`postgres-test`). The project will need a reproducible way to start PostgreSQL.
   - What's unclear: Whether to add a `docker-compose.yml` to the project or rely on the existing container.
   - Recommendation: Add a `docker-compose.yml` at the project root for reproducible setup. This is Claude's discretion since it is infrastructure tooling that supports the data layer.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java 21 | Compilation, runtime | Yes | 21.0.7 LTS | -- |
| Maven | Build system | Yes (IntelliJ bundled) | 3.9.11 | Maven wrapper (mvnw) -- MUST generate |
| PostgreSQL | Data storage | Yes (Docker container) | 18 (latest) | Docker Compose file in project |
| Docker | PostgreSQL hosting | Yes | 28.0.4 | -- |
| Docker Compose | Reproducible DB setup | Yes | v2.34.0 | -- |

**Missing dependencies with no fallback:**
- None -- all required tools are available.

**Missing dependencies with fallback:**
- Maven is not on system PATH (only available via IntelliJ bundled installation at `C:/Program Files/JetBrains/IntelliJ IDEA 2024.3.5/plugins/maven/lib/maven3/bin/mvn`). The Maven wrapper (`mvnw`/`mvnw.cmd`) MUST be generated at the project root so builds work from any terminal without IntelliJ.

**Note on PostgreSQL:** A Docker container named `postgres-test` is running on port 5432 with user `testuser`, password `testpass`, and database `testdb`. The plan should create a dedicated database for this project or add a Docker Compose file.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | Not in scope for Phase 1 (future AUTH requirements) |
| V3 Session Management | No | Not in scope for Phase 1 |
| V4 Access Control | No | Not in scope for Phase 1 |
| V5 Input Validation | No | Phase 1 has no endpoints; validation is Phase 3 (QUAL-01) |
| V6 Cryptography | No | No crypto operations in data layer |

### Known Threat Patterns for Spring Boot + PostgreSQL Data Layer

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| SQL injection via raw queries | Tampering | Use Spring Data JPA (parameterized queries) -- no raw SQL in repository layer [ASSUMED] |
| Database credential exposure | Information Disclosure | Use environment variables for datasource credentials, never hardcode in application.yml checked into git [ASSUMED] |
| Flyway migration tampering | Tampering | Migration checksums verified by Flyway on each run -- do not modify applied migrations [ASSUMED] |

**Phase 1 security impact is low:** No REST endpoints, no user input, no authentication. The primary security concern is database credential management, which is handled by using environment variable placeholders in `application.yml` with safe defaults for local development only.

## Sources

### Primary (HIGH confidence)
- [Spring Multi-Module Guide](https://spring.io/guides/gs/multi-module/) -- Multi-module Maven project structure, parent POM configuration, spring-boot-maven-plugin placement
- [MapStruct 1.6.3 Reference Guide](https://mapstruct.org/documentation/stable/reference/html/) -- Mapper configuration, Maven setup, componentModel

### Secondary (MEDIUM confidence)
- [Spring Boot 3.3.13 Release](https://spring.io/blog/2025/06/19/spring-boot-3-3-13-available-now/) -- Version confirmation
- [Flyway PostgreSQL Support in Spring Boot 3.3](https://dev-solve.com/posts/4ae6b9e) -- flyway-database-postgresql requirement
- [MapStruct + Lombok Maven Config](https://bootify.io/spring-data/mapstruct-with-maven-and-lombok.html) -- Annotation processor ordering
- [Thorben Janssen: UUID Primary Keys with Hibernate](https://thorben-janssen.com/generate-uuids-primary-keys-hibernate/) -- Hibernate 6 UUID generation strategies
- [Flyway Best Practices with Spring Boot](https://rieckpil.de/howto-best-practices-for-flyway-and-hibernate-with-spring-boot/) -- ddl-auto=validate recommendation
- [lombok-mapstruct-binding on Maven Central](https://central.sonatype.com/artifact/org.projectlombok/lombok-mapstruct-binding) -- Version 0.2.0 confirmation

### Tertiary (LOW confidence)
- Web search results from Medium, DZone, GeeksForGeeks -- General pattern validation, not authoritative

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH -- All libraries are well-established, versions confirmed against official sources and Maven Central. Spring Boot 3.3.13, MapStruct 1.6.3, and Flyway 10 are documented and stable.
- Architecture: HIGH -- Multi-module Maven with Spring Boot is a thoroughly documented pattern from official Spring guides. Project structure follows established conventions.
- Pitfalls: HIGH -- All pitfalls are well-documented in community resources and official documentation. Annotation processor ordering and Flyway module requirements are confirmed by multiple authoritative sources.

**Research date:** 2026-06-19
**Valid until:** 2026-07-19 (30 days -- stable technologies, no fast-moving components)
