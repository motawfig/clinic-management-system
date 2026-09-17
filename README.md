# Clinic Management System

**Authors:**

- Engineer Mohammed Abu Hadi — المهندس محمد أبوهادي
- Engineer Hazem Al-Balqi — المهندس حازم البلقي

---

## Project Overview

This is an academic **Java Software Maintenance** project that models a clinic domain with patient, doctor, appointment, and medical record entities. The project demonstrates the complete software maintenance lifecycle applied to an appointment scheduling system, following recognized maintenance engineering practices.

The system includes both an automated test suite verifying core business invariants and an interactive console application for live demonstration.

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
- Interactive console runtime application (`App.java`)
- In-memory demonstration repositories (`com.clinic.repository.memory`)
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

## Running the Console Application

The project includes an interactive console application that demonstrates the domain models and SMR-001 conflict detection in real time.

### Execution Commands

```bash
mvn compile
java -cp target\classes com.clinic.App
```

Upon startup, the console loads initial demonstration data (2 patients and 2 doctors) and displays the main interactive menu:

```
========================================
CLINIC MANAGEMENT SYSTEM
========================================
1. Add Patient
2. Add Doctor
3. Schedule Appointment
4. Update Appointment
5. Cancel Appointment
6. View All Appointments
7. View Appointments by Doctor
8. Add Medical Record
9. View Demo Data
10. Exit
```

### Available Operations

| Option | Operation | Description |
|---|---|---|
| 1 | Add Patient | Registers a new patient with name, birth date, gender, and contact details |
| 2 | Add Doctor | Registers a clinical practitioner with name and medical specialization |
| 3 | Schedule Appointment | Schedules a new appointment with conflict validation for the selected doctor |
| 4 | Update Appointment | Modifies an existing appointment date, duration, or status with conflict protection |
| 5 | Cancel Appointment | Cancels and removes an appointment by its identifier |
| 6 | View All Appointments | Displays a formatted tabular list of all scheduled appointments |
| 7 | View Appointments by Doctor | Displays filtered appointments for a specific doctor |
| 8 | Add Medical Record | Records consultation diagnosis, treatment, and clinical notes for a patient |
| 9 | View Demo Data | Lists preloaded and currently registered patients, doctors, and appointments |
| 10 | Exit | Terminates the application session |

> [!NOTE]
> **In-Memory Storage Notice:** All runtime data is stored in memory via `com.clinic.repository.memory` implementations and is reset when the application terminates.

---

## Live SMR-001 Conflict Demonstration

The console runtime demonstrates doctor-scoped conflict detection live through the following test sequence:

1. **Step 1 — Initial Booking (Doctor 1):**
   - Doctor ID: `1` (Dr. Khaled)
   - Date/Time: `2026-09-20 10:00`
   - Duration: `30` minutes
   - Status: `SCHEDULED`
   - **Result:** **Allowed.** Appointment saved with interval `[10:00, 10:30)`.

2. **Step 2 — Overlapping Booking (Same Doctor):**
   - Doctor ID: `1` (Dr. Khaled)
   - Date/Time: `2026-09-20 10:15`
   - Duration: `30` minutes
   - Status: `SCHEDULED`
   - **Result:** **Rejected.** Overlaps existing booking `[10:00, 10:30)` for Dr. Khaled. The console catches `AppointmentConflictException` and displays an informative warning.

3. **Step 3 — Concurrent Booking (Different Doctor):**
   - Doctor ID: `2` (Dr. Amal)
   - Date/Time: `2026-09-20 10:15`
   - Duration: `30` minutes
   - Status: `SCHEDULED`
   - **Result:** **Allowed.** Conflict checking is scoped strictly to the specified doctor's calendar (`findByDoctorId`). Dr. Amal has no conflicting booking.

4. **Step 4 — Boundary-Touching Booking (Doctor 1):**
   - Doctor ID: `1` (Dr. Khaled)
   - Date/Time: `2026-09-20 10:30`
   - Duration: `30` minutes
   - Status: `SCHEDULED`
   - **Result:** **Allowed.** Starts exactly when the first appointment ends (`10:30 == 10:30`). Strict inequality comparisons permit contiguous bookings.

> [!IMPORTANT]
> All validation logic resides exclusively in [`AppointmentService.java`](src/main/java/com/clinic/service/AppointmentService.java). `App.java` contains no duplicate validation rules; it acts solely as the user interface delegating to the service layer.

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

The project follows a layered architecture with clear package separation adhering to the Dependency Inversion Principle:

| Package | Responsibility |
|---|---|
| `com.clinic.model` | Domain entities (`Patient`, `Doctor`, `Appointment`, `MedicalRecord`) and enums (`Gender`, `AppointmentStatus`) |
| `com.clinic.service` | Business services — `AppointmentService` encapsulates SMR-001 conflict detection logic |
| `com.clinic.repository` | Data access contracts — `GenericRepository<T, ID>` and domain-specific interfaces |
| `com.clinic.repository.memory` | In-memory repository implementations satisfying repository contracts for console demonstration |
| `com.clinic.exception` | Domain exceptions — `AppointmentConflictException` |
| `com.clinic.util` | Infrastructure — `DatabaseConfig` connection manager |
| `com.clinic` | Interactive console application entry point — `App.java` |

### Architectural Clarity: Runtime vs. Persistence

To avoid any ambiguity regarding the persistence tier, the system distinguishes clearly between:

1. **Current Console Runtime:**
   `App.java` assembles the interactive runtime by wiring each service (`PatientService`, `DoctorService`, `AppointmentService`, `MedicalRecordService`) to its corresponding in-memory repository implementation (`InMemoryPatientRepository`, `InMemoryDoctorRepository`, `InMemoryAppointmentRepository`, `InMemoryMedicalRecordRepository`).

2. **JDBC Production Persistence:**
   No concrete JDBC repository implementations are currently implemented in the codebase. All persistence interfaces exist as contracts ready for future database integration.

3. **Database Schema & Configuration:**
   `schema.sql` defines the relational DDL (including the SMR-001 `duration_minutes` column) and `DatabaseConfig.java` / `db.properties` manage connection properties. These artifacts are syntactically complete and verified, but they are not connected to the current console runtime.

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
| Phase 10 | Final Documentation | Repository preparation, bilingual documentation, and interactive console runtime |

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
    │   │   │   └── AppointmentConflictException.java
    │   │   ├── model/
    │   │   │   ├── Appointment.java
    │   │   │   ├── AppointmentStatus.java
    │   │   │   ├── Doctor.java
    │   │   │   ├── Gender.java
    │   │   │   ├── MedicalRecord.java
    │   │   │   └── Patient.java
    │   │   ├── repository/
    │   │   │   ├── GenericRepository.java
    │   │   │   ├── AppointmentRepository.java
    │   │   │   ├── DoctorRepository.java
    │   │   │   ├── MedicalRecordRepository.java
    │   │   │   ├── PatientRepository.java
    │   │   │   └── memory/
    │   │   │       ├── InMemoryAppointmentRepository.java
    │   │   │       ├── InMemoryDoctorRepository.java
    │   │   │       ├── InMemoryMedicalRecordRepository.java
    │   │   │       └── InMemoryPatientRepository.java
    │   │   ├── service/
    │   │   │   ├── AppointmentService.java
    │   │   │   ├── DoctorService.java
    │   │   │   ├── MedicalRecordService.java
    │   │   │   └── PatientService.java
    │   │   └── util/
    │   │       └── DatabaseConfig.java
    │   └── resources/
    │       ├── db.properties
    │       └── schema.sql
    └── test/java/com/clinic/
        ├── model/
        │   ├── AppointmentTest.java
        │   ├── DoctorTest.java
        │   ├── MedicalRecordTest.java
        │   └── PatientTest.java
        ├── repository/
        │   └── InMemoryAppointmentRepository.java
        └── service/
            └── AppointmentServiceTest.java
```

---

## License

Academic project — Clinic Management System.
