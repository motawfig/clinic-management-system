# Reverse Engineering Analysis

## Overview

**Phase:** Phase 9 — Reverse Engineering  
**Change Reference:** SMR-001 — Appointment Conflict Detection  
**Method:** Design recovery from source code (Chikofsky and Cross, 1990)

Reverse engineering recovers system design, architecture, and business rules directly from the implemented source code, treating the codebase as the primary source of truth.

---

## Recovered Package Structure

| Package | Contents | Dependencies |
|---|---|---|
| `com.clinic` | `App.java` (entry point) | `com.clinic.model` |
| `com.clinic.model` | Domain entities and enums | None |
| `com.clinic.repository` | Data access interfaces | `com.clinic.model` |
| `com.clinic.service` | Business services | `com.clinic.model`, `com.clinic.repository`, `com.clinic.exception` |
| `com.clinic.exception` | Domain exceptions | None |
| `com.clinic.util` | Infrastructure utilities | None |

---

## Recovered Domain Model

```
Patient (1) ◄──── (0..*) Appointment (0..*) ────► (1) Doctor
   │
   │ (1)
   └──── (0..*) MedicalRecord
```

### Relationship Details

- **Patient 1 ← 0..* Appointment:** Each appointment holds one patient reference. A patient may have zero or more appointments.
- **Doctor 1 ← 0..* Appointment:** Each appointment holds one doctor reference. A doctor may have zero or more appointments.
- **Patient 1 ← 0..* MedicalRecord:** Each medical record holds one patient reference. A patient may have zero or more medical records.

### Domain Model Invariants (Enforced by Entity Classes)

| Entity | Invariant | Mechanism |
|---|---|---|
| `Patient` | `fullName` not null/blank | `setFullName()` throws `IllegalArgumentException` |
| `Doctor` | `fullName` not null/blank | `setFullName()` throws `IllegalArgumentException` |
| `Appointment` | `patient` not null | `setPatient()` throws `IllegalArgumentException` |
| `Appointment` | `doctor` not null | `setDoctor()` throws `IllegalArgumentException` |
| `Appointment` | `durationMinutes > 0` | `setDurationMinutes()` throws `IllegalArgumentException` |
| `MedicalRecord` | `patient` not null | `setPatient()` throws `IllegalArgumentException` |

**Note:** `appointmentDateTime` is NOT null-checked in `Appointment.java`. The service layer (`validateAppointmentForConflictCheck`) provides this check before conflict detection.

---

## Recovered Repository Hierarchy

```
GenericRepository<T, ID>  (interface: save, findById, findAll, update, delete)
    ├── PatientRepository        (+ findByName)
    ├── DoctorRepository         (+ findBySpecialization)
    ├── AppointmentRepository    (+ findByPatientId, findByDoctorId, findByDate)
    └── MedicalRecordRepository  (+ findByPatientId)
```

- All repository contracts are **interfaces** in `com.clinic.repository`
- At the time of Phase 9, `InMemoryAppointmentRepository` in `src/test/java` was the sole concrete implementation

---

## Recovered Service Architecture

| Service | Repository | Pattern |
|---|---|---|
| `PatientService` | `PatientRepository` | Pass-through delegation |
| `DoctorService` | `DoctorRepository` | Pass-through delegation |
| `AppointmentService` | `AppointmentRepository` | Business logic (conflict detection) |
| `MedicalRecordService` | `MedicalRecordRepository` | Pass-through delegation |

**Cross-service coupling:** Zero. No service imports or depends on another service.

---

## Recovered Appointment Conflict Business Rules

| Rule | Statement | Source Evidence |
|---|---|---|
| BR-01 | Duration must be > 0 | `Appointment.setDurationMinutes()` — `IllegalArgumentException` |
| BR-02 | Only SCHEDULED and CONFIRMED block time | `isBlockingStatus()` |
| BR-03 | COMPLETED, CANCELLED, NO_SHOW do not block | `conflictsWith()` returns false for non-blocking |
| BR-04 | Conflict checking is doctor-scoped | `findByDoctorId(doctorId)` |
| BR-05 | Intervals are half-open: [start, start + duration) | `calculateEndTime()` |
| BR-06 | Overlap: newStart < existingEnd AND newEnd > existingStart | `appointmentsOverlap()` |
| BR-07 | Boundary-touching is allowed | Strict inequalities: equal endpoints → no overlap |
| BR-08 | Schedule checks conflict before save | `ensureNoAppointmentConflictForScheduling()` precedes `save()` |
| BR-09 | Update checks conflict before update | `ensureNoAppointmentConflictForUpdate()` precedes `update()` |
| BR-10 | Update self-excludes by ID comparison | `isCurrentAppointment()` |

---

## Database Schema Artifact

`schema.sql` defines four tables aligned with the domain model:

| Entity | Table | Key Columns |
|---|---|---|
| `Patient` | `patients` | `id`, `full_name`, `date_of_birth`, `gender`, `phone`, `email`, `address` |
| `Doctor` | `doctors` | `id`, `full_name`, `specialization`, `phone`, `email` |
| `Appointment` | `appointments` | `id`, `patient_id` (FK), `doctor_id` (FK), `appointment_date_time`, `duration_minutes`, `status`, `notes` |
| `MedicalRecord` | `medical_records` | `id`, `patient_id` (FK), `diagnosis`, `treatment`, `notes`, `created_at` |

**Important:** `schema.sql` is a design artifact. No JDBC repository implementation maps domain entities to these tables at runtime. Domain entities are NOT currently persisted to MySQL.

---

## Historical Architectural Gaps (Phase 9 Baseline)

At the time the Phase 9 reverse engineering analysis was performed on the post-refactoring baseline:

1. **No production JDBC persistence** — All repository contracts were interfaces with no concrete JDBC implementations in `src/main/java`.
2. **DatabaseConfig was unreferenced** — `DatabaseConfig.java` and `db.properties` existed but were not called by any service or repository.
3. **App.java was unwired** — The entry point created sample model objects for stdout demonstration without assembling the service graph.
4. **Pass-through services lacked validation** — `PatientService`, `DoctorService`, and `MedicalRecordService` performed no business validation before delegating to repositories.

---

## Post-Analysis Console Runtime Enhancement

Following the Phase 9 analysis, the application entry point was enhanced into an interactive console runner, introducing concrete runtime wiring:

- **Explicit Runtime Wiring in `App.java`:** Rather than remaining a static demonstration script, `App.java` now explicitly instantiates all four services and injects corresponding in-memory repositories (`InMemoryPatientRepository`, `InMemoryDoctorRepository`, `InMemoryAppointmentRepository`, `InMemoryMedicalRecordRepository`).
- **Preserved Abstraction Boundaries:** The fundamental architectural pattern remains `service → repository interface`. Services still depend exclusively on interfaces in `com.clinic.repository`, completely unaware of the underlying storage mechanism.
- **In-Memory Runtime Persistence:** The runtime repository implementations (`src/main/java/com/clinic/repository/memory/`) store entities in volatile Java collections. Data persists during the execution session and resets upon exit.
- **JDBC Persistence Remains Unimplemented:** Concrete JDBC persistence against a live MySQL database remains unbuilt. `DatabaseConfig.java`, `db.properties`, and `schema.sql` remain in place as valid configuration templates for future database connectivity.
