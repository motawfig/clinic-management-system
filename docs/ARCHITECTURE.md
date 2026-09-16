# System Architecture

## Overview

The Clinic Management System follows a **Layered Architecture (N-Tier)** pattern with structural adherence to the **Dependency Inversion Principle (DIP)**. Services depend on repository interfaces rather than concrete implementations.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     APPLICATION / ENTRY LAYER                                │
│                                                                              │
│  App.java (Demo harness — prints sample models to stdout)                   │
│  NOTE: Does NOT instantiate services or repositories.                       │
│        Does NOT wire the runtime dependency graph.                          │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       : (Conceptual / not wired at runtime)
                                       v
┌──────────────────────────────────────────────────────────────────────────────┐
│                     SERVICE LAYER (Parallel, Independent)                    │
│                                                                              │
│  PatientService       ───► PatientRepository       (pass-through)           │
│  DoctorService        ───► DoctorRepository        (pass-through)           │
│  AppointmentService   ───► AppointmentRepository   (conflict detection)     │
│  MedicalRecordService ───► MedicalRecordRepository (pass-through)           │
│                                                                              │
│  Cross-service coupling: ZERO                                               │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │ (depends on interfaces only)
┌──────────────────────────────────────▼───────────────────────────────────────┐
│                     REPOSITORY ABSTRACTION LAYER                             │
│                                                                              │
│  GenericRepository<T, ID>  (base interface: save, findById, findAll, etc.)  │
│    ├── PatientRepository        (+ findByName)                              │
│    ├── DoctorRepository         (+ findBySpecialization)                    │
│    ├── AppointmentRepository    (+ findByPatientId, findByDoctorId, etc.)   │
│    └── MedicalRecordRepository  (+ findByPatientId)                        │
└──────────────┬───────────────────────────────────┬───────────────────────────┘
               │                                   │
   (Production)│                       (Test scope) │
┌──────────────▼──────────────┐  ┌─────────────────▼──────────────────────────┐
│  JDBC Persistence Layer     │  │  Test Infrastructure                       │
│                             │  │                                            │
│  [ NOT IMPLEMENTED ]        │  │  InMemoryAppointmentRepository             │
│                             │  │  (LinkedHashMap-backed test double)        │
└─────────────────────────────┘  └────────────────────────────────────────────┘
               :
               : (Prepared but not wired)
┌──────────────▼──────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE / CONFIGURATION                          │
│                                                                              │
│  DatabaseConfig.java   db.properties   schema.sql                           │
│  (Connection utility)  (JDBC template)  (MySQL DDL)                         │
│                                                                              │
│  NOTE: These exist but are not referenced by any production service         │
│        or repository class.                                                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Service Architecture

### Independent Services (Zero Cross-Coupling)

Each service is completely independent. No service imports, references, or depends on another service:

| Service | Repository | Pattern |
|---|---|---|
| `PatientService` | `PatientRepository` | Pass-through delegation |
| `DoctorService` | `DoctorRepository` | Pass-through delegation |
| `AppointmentService` | `AppointmentRepository` | Business logic (SMR-001 conflict detection) |
| `MedicalRecordService` | `MedicalRecordRepository` | Pass-through delegation |

### AppointmentService — Conflict Detection Engine

`AppointmentService` is the only service that contains business logic beyond simple delegation. It implements:

1. **Pre-save conflict check** in `scheduleAppointment()`
2. **Pre-update conflict check** with self-exclusion in `updateAppointment()`
3. **Doctor-scoped calendar querying** via `findByDoctorId()`
4. **Status-aware filtering** — only `SCHEDULED` and `CONFIRMED` block time
5. **Half-open interval overlap detection** — `[start, start + durationMinutes)`

---

## Repository Architecture

```
GenericRepository<T, ID>  (interface)
    │
    ├── PatientRepository       (interface — adds findByName)
    ├── DoctorRepository        (interface — adds findBySpecialization)
    ├── AppointmentRepository   (interface — adds findByPatientId, findByDoctorId, findByDate)
    └── MedicalRecordRepository (interface — adds findByPatientId)
                                         │
                                         └── InMemoryAppointmentRepository (test double, src/test/java)
```

---

## Important Architectural Limitations

### 1. No Production JDBC Repository Implementations

All five repository interfaces (`GenericRepository`, `PatientRepository`, `DoctorRepository`, `AppointmentRepository`, `MedicalRecordRepository`) exist **only as interfaces** in `src/main/java`. No concrete JDBC implementations exist in the production source tree.

### 2. DatabaseConfig and Schema Exist but Are Not Wired

`DatabaseConfig.java` loads `db.properties` and can provide a JDBC `Connection`. `schema.sql` defines a valid MySQL schema. However, no production class in `com.clinic.service` or `com.clinic.repository` references `DatabaseConfig` or executes SQL against the schema.

### 3. App.java Does Not Assemble the Runtime Service Graph

`App.java` instantiates domain model objects directly using `new` and prints them to `System.out`. It does not:
- Create any service instances
- Create any repository instances
- Wire dependencies between layers
- Execute any business workflows

### 4. InMemoryAppointmentRepository Is Test-Only

The only concrete repository implementation in the entire codebase is `InMemoryAppointmentRepository`, which resides in `src/test/java` and uses a `LinkedHashMap` for in-memory storage during test execution.

---

## Domain Model

```
Patient (1) ◄────── (0..*) Appointment (0..*) ──────► (1) Doctor
   │
   │ (1)
   │
   └────── (0..*) MedicalRecord
```

- Each `Appointment` references exactly one `Patient` and one `Doctor`
- Each `MedicalRecord` references exactly one `Patient`
- A `Patient` or `Doctor` may have zero or more appointments
- A `Patient` may have zero or more medical records

---

## Dependency Flow

```
AppointmentService
    │
    ├── depends on: AppointmentRepository (interface)
    ├── depends on: Appointment (model)
    ├── depends on: AppointmentStatus (enum)
    ├── depends on: AppointmentConflictException (exception)
    │
    └── does NOT depend on: PatientService, DoctorService, MedicalRecordService,
                            DatabaseConfig, JDBC, SQL
```
