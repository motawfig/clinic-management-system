# Project Structure

## Overview

The Clinic Management System follows a standard Maven project layout with a layered Java architecture adhering to the Dependency Inversion Principle.

---

## Directory Tree

```
clinic-management-system/
├── pom.xml                                          Build configuration
├── README.md                                        Project overview and console guide
├── CHANGELOG.md                                     Change history
├── docs/                                            Project documentation
│   ├── PROJECT_STRUCTURE.md                         This file
│   ├── MAINTENANCE_PROCESS.md                       Software maintenance lifecycle
│   ├── ARCHITECTURE.md                              System architecture
│   ├── TEST_EVIDENCE.md                             Test results and verification
│   ├── PROGRAM_SLICING.md                           Program slicing analysis
│   ├── REVERSE_ENGINEERING.md                       Reverse engineering findings
│   └── DISCUSSION_GUIDE.md                          Bilingual discussion/viva guide
└── src/
    ├── main/
    │   ├── java/com/clinic/
    │   │   ├── App.java                             Interactive console runtime entry point
    │   │   ├── exception/
    │   │   │   └── AppointmentConflictException.java  Domain exception for calendar collisions
    │   │   ├── model/
    │   │   │   ├── Appointment.java                 Appointment entity with duration
    │   │   │   ├── AppointmentStatus.java           Status enum (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW)
    │   │   │   ├── Doctor.java                      Doctor entity
    │   │   │   ├── Gender.java                      Gender enum (MALE, FEMALE, OTHER)
    │   │   │   ├── MedicalRecord.java               Medical record entity
    │   │   │   └── Patient.java                     Patient entity
    │   │   ├── repository/
    │   │   │   ├── GenericRepository.java           Generic CRUD interface
    │   │   │   ├── AppointmentRepository.java       Appointment data access interface
    │   │   │   ├── DoctorRepository.java            Doctor data access interface
    │   │   │   ├── MedicalRecordRepository.java     Medical record data access interface
    │   │   │   ├── PatientRepository.java           Patient data access interface
    │   │   │   └── memory/                          In-memory runtime demonstration repositories
    │   │   │       ├── InMemoryAppointmentRepository.java
    │   │   │       ├── InMemoryDoctorRepository.java
    │   │   │       ├── InMemoryMedicalRecordRepository.java
    │   │   │       └── InMemoryPatientRepository.java
    │   │   ├── service/
    │   │   │   ├── AppointmentService.java          Conflict detection + scheduling (SMR-001)
    │   │   │   ├── DoctorService.java               Pass-through doctor operations
    │   │   │   ├── MedicalRecordService.java        Pass-through medical record operations
    │   │   │   └── PatientService.java              Pass-through patient operations
    │   │   └── util/
    │   │       └── DatabaseConfig.java              Database connection configuration
    │   └── resources/
    │       ├── db.properties                        Database connection template
    │       └── schema.sql                           MySQL DDL schema
    └── test/java/com/clinic/
        ├── model/
        │   ├── AppointmentTest.java                 9 tests — appointment invariants and duration
        │   ├── DoctorTest.java                      7 tests — doctor invariants
        │   ├── MedicalRecordTest.java               6 tests — medical record invariants
        │   └── PatientTest.java                     8 tests — patient invariants
        ├── repository/
        │   └── InMemoryAppointmentRepository.java   Test double for isolated service testing
        └── service/
            └── AppointmentServiceTest.java          12 tests — conflict detection and scheduling
```

---

## Package Responsibilities

### `com.clinic` (Root)

Contains `App.java`, the interactive console application entry point. `App.java` assembles the console runtime by wiring the service layer (`PatientService`, `DoctorService`, `AppointmentService`, `MedicalRecordService`) to in-memory repository implementations, loads initial seed data, and renders a 10-option interactive menu for live operation.

### `com.clinic.model`

Domain entities and value types. Completely decoupled from repositories, services, and infrastructure.

- **`Patient`** — patient demographics (name, date of birth, gender, contact, address)
- **`Doctor`** — clinical practitioner (name, specialization, contact)
- **`Appointment`** — appointment record (patient, doctor, datetime, durationMinutes, status, notes)
- **`MedicalRecord`** — clinical consultation record (patient, diagnosis, treatment, notes, timestamp)
- **`Gender`** — enum: `MALE`, `FEMALE`, `OTHER`
- **`AppointmentStatus`** — enum: `SCHEDULED`, `CONFIRMED`, `COMPLETED`, `CANCELLED`, `NO_SHOW`

### `com.clinic.repository`

Data access interfaces following the Repository pattern with Dependency Inversion.

- **`GenericRepository<T, ID>`** — base CRUD contract: `save`, `findById`, `findAll`, `update`, `delete`
- **`AppointmentRepository`** — extends generic; adds `findByPatientId`, `findByDoctorId`, `findByDate`
- **`PatientRepository`** — extends generic; adds `findByName`
- **`DoctorRepository`** — extends generic; adds `findBySpecialization`
- **`MedicalRecordRepository`** — extends generic; adds `findByPatientId`

**Persistence Status:** All contracts under `com.clinic.repository` are pure Java interfaces. No concrete JDBC production persistence is implemented.

### `com.clinic.repository.memory`

Runtime demonstration repository implementations. These classes satisfy the repository interfaces using in-memory Java collections (`LinkedHashMap`, `ConcurrentHashMap`, or `HashMap` with atomic ID generators):

- **`InMemoryPatientRepository`** — implements `PatientRepository` for console runtime
- **`InMemoryDoctorRepository`** — implements `DoctorRepository` for console runtime
- **`InMemoryAppointmentRepository`** — implements `AppointmentRepository` for console runtime (supports `findByDoctorId`, `findByPatientId`, `findByDate`)
- **`InMemoryMedicalRecordRepository`** — implements `MedicalRecordRepository` for console runtime

> [!NOTE]
> These are runtime demonstration repositories using in-memory collections. They do **not** provide database persistence, and all data is lost when the program terminates.

### `com.clinic.service`

Application services. Each service injects its respective repository interface via constructor.

- **`AppointmentService`** — the principal maintenance component for SMR-001. Contains conflict detection logic, scheduling, update with self-exclusion, cancellation, and calendar queries.
- **`PatientService`** — pass-through delegation to `PatientRepository`
- **`DoctorService`** — pass-through delegation to `DoctorRepository`
- **`MedicalRecordService`** — pass-through delegation to `MedicalRecordRepository`

### `com.clinic.exception`

Domain-specific exceptions.

- **`AppointmentConflictException`** — unchecked `RuntimeException` thrown when a scheduling or update operation detects a blocking time overlap for the same doctor.

### `com.clinic.util`

Infrastructure utilities.

- **`DatabaseConfig`** — static utility that loads `db.properties` and provides `getConnection()`. Prepared for future database integration; not connected to the current console runtime.

### `src/main/resources`

- **`db.properties`** — database connection template (`jdbc:mysql://localhost:3306/clinic_db`)
- **`schema.sql`** — MySQL DDL defining `patients`, `doctors`, `appointments` (with `duration_minutes`), and `medical_records` tables

---

## Principal Maintenance Component

**`AppointmentService.java`** is the central component modified for SMR-001. It contains:

- `scheduleAppointment()` — validates conflict before `save()`
- `updateAppointment()` — validates conflict (with self-exclusion) before `update()`
- `ensureNoAppointmentConflictForScheduling()` — prepares conflict check for new bookings
- `ensureNoAppointmentConflictForUpdate()` — prepares conflict check for modifications with self-exclusion
- `ensureNoAppointmentConflict()` — central conflict detection coordination
- `validateAppointmentForConflictCheck()` — precondition validation (non-null appointment, doctor, datetime)
- `isCurrentAppointment()` — ID-based self-exclusion predicate
- `conflictsWith()` — status filter + overlap comparator
- `calculateEndTime()` — `dateTime.plusMinutes(durationMinutes)`
- `isBlockingStatus()` — identifies `SCHEDULED` and `CONFIRMED`
- `appointmentsOverlap()` — strict half-open interval overlap check
