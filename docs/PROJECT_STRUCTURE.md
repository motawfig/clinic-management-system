# Project Structure

## Overview

The Clinic Management System follows a standard Maven project layout with a layered Java architecture.

---

## Directory Tree

```
clinic-management-system/
├── pom.xml                                          Build configuration
├── README.md                                        Project overview
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
    │   │   ├── App.java                             Application entry point (demo harness)
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
    │   │   │   └── PatientRepository.java           Patient data access interface
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
        │   └── InMemoryAppointmentRepository.java   Test double (LinkedHashMap-backed)
        └── service/
            └── AppointmentServiceTest.java          12 tests — conflict detection and scheduling
```

---

## Package Responsibilities

### `com.clinic` (Root)

Contains `App.java`, the application entry point. Currently serves as a demonstration harness that instantiates sample domain models and prints them to stdout. It does not assemble or wire the service-repository object graph.

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

**Note:** All repositories in `src/main/java` are interfaces only. No production JDBC implementations exist.

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

- **`DatabaseConfig`** — static utility that loads `db.properties` and provides `getConnection()`. Currently unreferenced by any production class.

### `src/main/resources`

- **`db.properties`** — database connection template (`jdbc:mysql://localhost:3306/clinic_db`)
- **`schema.sql`** — MySQL DDL defining `patients`, `doctors`, `appointments` (with `duration_minutes`), and `medical_records` tables

---

## Principal Maintenance Component

**`AppointmentService.java`** is the central component modified for SMR-001. It contains:

- `scheduleAppointment()` — validates conflict before `save()`
- `updateAppointment()` — validates conflict (with self-exclusion) before `update()`
- `ensureNoAppointmentConflict()` — central conflict detection path
- `validateAppointmentForConflictCheck()` — precondition validation
- `isCurrentAppointment()` — self-exclusion predicate
- `conflictsWith()` — status filter + overlap comparator
- `calculateEndTime()` — `dateTime.plusMinutes(durationMinutes)`
- `isBlockingStatus()` — identifies `SCHEDULED` and `CONFIRMED`
- `appointmentsOverlap()` — strict half-open interval overlap check
