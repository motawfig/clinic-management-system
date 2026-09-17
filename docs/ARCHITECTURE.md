# System Architecture

## Overview

The Clinic Management System follows a **Layered Architecture (N-Tier)** pattern with structural adherence to the **Dependency Inversion Principle (DIP)**. Services depend exclusively on repository interfaces rather than concrete data access implementations.

---

## Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                    INTERACTIVE CONSOLE RUNTIME (App.java)                    │
│                                                                              │
│  Wires the service layer to in-memory repositories for interactive demo.     │
│  Provides 10-option CLI menu; loads seed data into memory.                   │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │ (instantiates & injects)
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
  (Console Demo Runtime)               (Automated Test Scope)
┌──────────────▼──────────────────────────┐  ┌─────▼──────────────────────────┐
│ In-Memory Demo Repositories             │  │ Test Infrastructure            │
│ (src/main/.../repository/memory/)       │  │ (src/test/.../repository/)     │
│                                         │  │                                │
│ • InMemoryPatientRepository             │  │ • InMemoryAppointmentRepository│
│ • InMemoryDoctorRepository              │  │   (LinkedHashMap double for    │
│ • InMemoryAppointmentRepository         │  │    isolated unit testing)      │
│ • InMemoryMedicalRecordRepository       │  └────────────────────────────────┘
│                                         │
│ (In-memory collections; non-persistent) │
└─────────────────────────────────────────┘
               :
               : (Prepared but not connected to current runtime)
┌──────────────▼──────────────────────────────────────────────────────────────┐
│                     INFRASTRUCTURE / CONFIGURATION                          │
│                                                                              │
│  DatabaseConfig.java   db.properties   schema.sql                           │
│  (Connection utility)  (JDBC template)  (MySQL DDL)                         │
│                                                                              │
│  JDBC Production Persistence: NOT IMPLEMENTED                               │
│  No persistent database connection is active in the console runtime.        │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## Runtime Execution Flow

When the interactive console application executes:

```
App.java (Console Entry Point)
   │
   ├── 1. Instantiates In-Memory Repositories (com.clinic.repository.memory.*)
   │        ├── InMemoryPatientRepository
   │        ├── InMemoryDoctorRepository
   │        ├── InMemoryAppointmentRepository
   │        └── InMemoryMedicalRecordRepository
   │
   ├── 2. Injects Repositories into Parallel Services (com.clinic.service.*)
   │        ├── new PatientService(inMemoryPatientRepo)
   │        ├── new DoctorService(inMemoryDoctorRepo)
   │        ├── new AppointmentService(inMemoryApptRepo)
   │        └── new MedicalRecordService(inMemoryMedRecordRepo)
   │
   ├── 3. Seeds Initial Demo Data (2 patients, 2 doctors)
   │
   └── 4. Renders Interactive Menu & Delegates User Actions to Services
            └── e.g., scheduleAppointment() calls AppointmentService
                      └── AppointmentService executes SMR-001 conflict check
                                ├── Overlap detected  → Throws AppointmentConflictException
                                └── No overlap        → Saves to in-memory collection
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
    │     └── InMemoryPatientRepository (src/main/java/com/clinic/repository/memory/)
    │
    ├── DoctorRepository        (interface — adds findBySpecialization)
    │     └── InMemoryDoctorRepository (src/main/java/com/clinic/repository/memory/)
    │
    ├── AppointmentRepository   (interface — adds findByPatientId, findByDoctorId, findByDate)
    │     ├── InMemoryAppointmentRepository (src/main/java/com/clinic/repository/memory/)
    │     └── InMemoryAppointmentRepository (src/test/java/com/clinic/repository/ [test double])
    │
    └── MedicalRecordRepository (interface — adds findByPatientId)
          └── InMemoryMedicalRecordRepository (src/main/java/com/clinic/repository/memory/)
```

---

## Architectural Distinctions & Known Limitations

To maintain rigorous architectural accuracy, the system distinguishes clearly between the following tiers:

### 1. Current Console Runtime (Implemented)
`App.java` now assembles the console runtime by wiring the service layer to in-memory repository implementations (`src/main/java/com/clinic/repository/memory/`). This enables live interactive demonstration of patient/doctor management, appointment scheduling, and SMR-001 conflict detection. All data resides in volatile in-memory collections and resets upon program exit.

### 2. JDBC Production Persistence (Not Implemented)
No JDBC production persistence is implemented. All repository abstractions exist strictly as Java interfaces in `com.clinic.repository`. No concrete classes implement SQL queries or JDBC statements in the production source tree.

### 3. Database Schema and Configuration (Prepared but Disconnected)
`DatabaseConfig.java` loads `db.properties` and can supply a JDBC `Connection`. `schema.sql` defines the relational DDL including the `duration_minutes` column. However, no production service or repository references `DatabaseConfig`, and no persistent database connection is active during application runtime.

### 4. Test Double Isolation
`src/test/java/com/clinic/repository/InMemoryAppointmentRepository.java` serves as a dedicated test double for automated unit and service testing, ensuring tests run isolated from the console runtime state and external databases.

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
