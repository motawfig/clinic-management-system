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

- All repositories are **interfaces only** in production code
- `InMemoryAppointmentRepository` is the sole concrete implementation, residing in `src/test/java`

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

## Recovered Architectural Gaps

1. **No production JDBC persistence** — All repositories are interfaces. No concrete JDBC implementations exist in `src/main/java`.
2. **DatabaseConfig is unreferenced** — `DatabaseConfig.java` and `db.properties` exist but are not called by any service or repository.
3. **App.java is disconnected** — The entry point creates sample model objects for stdout demonstration. It does not instantiate services, repositories, or wire dependencies.
4. **Pass-through services lack validation** — `PatientService`, `DoctorService`, and `MedicalRecordService` perform no business validation before delegating to repositories.
