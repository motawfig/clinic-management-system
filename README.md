# Clinic Management System

**Authors:**

- Engineer Mohammed Abu Hadi — المهندس محمد أبوهادي
- Engineer Hazem Al-Balqi — المهندس حازم البلقي

---

## Project Overview

This is an academic **Java Software Maintenance** project that models a clinic domain with patient, doctor, appointment, and medical record entities. The project demonstrates the complete software maintenance lifecycle applied to an appointment scheduling system, following recognized maintenance engineering practices.

---

## Maintenance Objective

**SMR-001 — Appointment Conflict Detection**

The primary maintenance change request adds doctor-scoped appointment conflict detection to prevent double-booking. The system validates appointment time intervals before scheduling or updating, and raises `AppointmentConflictException` when a blocking overlap is detected.

---

## Implemented Features

- Patient model with demographics and validation
- Doctor model with specialization
- Appointment model with configurable duration
- MedicalRecord model for clinical notes
- Generic repository abstraction (`GenericRepository<T, ID>`)
- Domain-specific repository interfaces (`AppointmentRepository`, `PatientRepository`, `DoctorRepository`, `MedicalRecordRepository`)
- Service layer (`PatientService`, `DoctorService`, `AppointmentService`, `MedicalRecordService`)
- Appointment duration support (`durationMinutes`, default 30)
- Doctor-scoped conflict detection via `findByDoctorId`
- Conflict protection during `scheduleAppointment()` and `updateAppointment()`
- Status-aware conflict rules (blocking vs. non-blocking)
- Self-exclusion during update operations
- Automated JUnit 5 test suite (42 tests)

---

## Appointment Conflict Rules

### Blocking Statuses (prevent overlapping bookings)

- `SCHEDULED`
- `CONFIRMED`

### Non-Blocking Statuses (do not block time slots)

- `COMPLETED`
- `CANCELLED`
- `NO_SHOW`

### Interval Model

Appointments use a half-open interval:

```
[start, start + durationMinutes)
```

### Overlap Formula

```
newStart < existingEnd && newEnd > existingStart
```

### Boundary Rule

Boundary-touching appointments (where one appointment ends exactly when another begins) are **allowed** and do **not** conflict. This follows naturally from the strict inequality comparisons in the overlap formula.

---

## Technologies

| Technology | Version |
|---|---|
| Java | 21 (LTS) |
| Maven | 3.9.x |
| JUnit Jupiter | 5.10.2 |
| MySQL Connector/J | 8.3.0 |
| Maven Compiler Plugin | 3.13.0 |
| Maven Surefire Plugin | 3.2.5 |

---

## Build and Test

```bash
mvn clean test
```

**Verified result:**

```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## Architecture

The project follows a layered architecture with clear package separation:

| Package | Responsibility |
|---|---|
| `com.clinic.model` | Domain entities (`Patient`, `Doctor`, `Appointment`, `MedicalRecord`) and enums (`Gender`, `AppointmentStatus`) |
| `com.clinic.service` | Business services — `AppointmentService` contains conflict detection logic |
| `com.clinic.repository` | Data access interfaces — `GenericRepository<T, ID>` base with domain-specific extensions |
| `com.clinic.exception` | Domain exceptions — `AppointmentConflictException` |
| `com.clinic.util` | Infrastructure — `DatabaseConfig` for connection configuration |
| `com.clinic` | Application entry point — `App.java` |

### Important Architectural Notes

- **Production JDBC repository implementations are NOT currently implemented.** All repositories exist only as Java interfaces in `src/main/java`.
- **`InMemoryAppointmentRepository`** exists only under test scope (`src/test/java`) as a `LinkedHashMap`-backed test double.
- **`App.java`** currently acts as a demonstration entry point that instantiates sample domain models and prints them to stdout. It does **not** wire the complete service/repository/database runtime graph.
- **`DatabaseConfig.java`**, **`db.properties`**, and **`schema.sql`** are present and syntactically valid but are not referenced by any production service or repository class.

---

## Software Maintenance Work

This project documents a complete software maintenance lifecycle:

| Phase | Name | Description |
|---|---|---|
| Phase 1 | Baseline | Initial project foundation — domain models, repositories, services, 27 baseline tests |
| Phase 2 | SMR | Formal Software Modification Request — SMR-001 specification with acceptance criteria AC-01 through AC-08 |
| Phase 3 | Impact Analysis | Change point identification, ripple effect assessment, and design decisions |
| Phase 4 | Implementation & Testing | Production code changes, `AppointmentConflictException`, conflict detection, 42 tests passing |
| Phase 5 | Maintenance Classification | Classification as Perfective Maintenance (primary) with Preventive characteristics (secondary) |
| Phase 6 | Code Translation | Static semantic translation of conflict logic from Java 21 to Python 3 |
| Phase 7 | Refactoring | Decomposition of conflict validation into cohesive helper methods |
| Phase 8 | Program Slicing | Static backward and forward slicing of conflict detection dependencies |
| Phase 9 | Reverse Engineering | Design recovery from source code — architecture, domain model, business rules |
| Phase 10 | Final Documentation | Repository preparation, bilingual documentation, and GitHub publication |

### Maintenance Classification

- **Primary:** Perfective Maintenance — enhancing the system with new conflict detection capability
- **Secondary characteristics:** Preventive Maintenance — guarding against future scheduling data integrity issues

---

## Project Structure

```
clinic-management-system/
├── pom.xml
├── README.md
├── CHANGELOG.md
├── docs/
│   ├── PROJECT_STRUCTURE.md
│   ├── MAINTENANCE_PROCESS.md
│   ├── ARCHITECTURE.md
│   ├── TEST_EVIDENCE.md
│   ├── PROGRAM_SLICING.md
│   ├── REVERSE_ENGINEERING.md
│   └── DISCUSSION_GUIDE.md
└── src/
    ├── main/
    │   ├── java/com/clinic/
    │   │   ├── App.java
    │   │   ├── exception/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   ├── service/
    │   │   └── util/
    │   └── resources/
    │       ├── db.properties
    │       └── schema.sql
    └── test/java/com/clinic/
        ├── model/
        ├── repository/
        └── service/
```

---

## License

Academic project — Clinic Management System.
