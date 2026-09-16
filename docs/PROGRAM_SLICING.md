# Program Slicing Analysis

## Overview

**Phase:** Phase 8 — Program Slicing  
**Change Reference:** SMR-001 — Appointment Conflict Detection  
**Target:** `AppointmentService.java` (post-refactoring)  
**Analysis Type:** Static program slicing  
**Dynamic slicing:** NOT performed

Program slicing extracts the subset of program statements that affect (backward slice) or are affected by (forward slice) a chosen variable or statement — the **slicing criterion**.

---

## Backward Slice

### Slicing Criterion

**Statement:** `throw new AppointmentConflictException(...)`  
**Location:** `AppointmentService.ensureNoAppointmentConflict()`

### Question

Which variables and statements determine whether `AppointmentConflictException` is thrown?

### Dependency Chain

```
appointment (input parameter)
    │
    ├── appointment.getDoctor().getId()  →  doctorId
    │       │
    │       └── appointmentRepository.findByDoctorId(doctorId)  →  List<Appointment>
    │               │
    │               └── for each existingAppointment:
    │                       │
    │                       ├── isCurrentAppointment(existing, current)
    │                       │       └── currentAppointment (null for schedule, self for update)
    │                       │           └── existingAppointment.getId() == currentAppointment.getId()
    │                       │
    │                       └── conflictsWith(newStart, newEnd, existingAppointment)
    │                               │
    │                               ├── existingAppointment.getStatus()
    │                               │       └── isBlockingStatus(status)
    │                               │           └── status == SCHEDULED || status == CONFIRMED
    │                               │
    │                               ├── newStart = appointment.getAppointmentDateTime()
    │                               ├── newEnd = calculateEndTime(appointment)
    │                               │       └── appointment.getDurationMinutes()
    │                               ├── existingStart = existing.getAppointmentDateTime()
    │                               ├── existingEnd = calculateEndTime(existing)
    │                               │       └── existing.getDurationMinutes()
    │                               │
    │                               └── appointmentsOverlap(newStart, newEnd, existingStart, existingEnd)
    │                                       └── newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart)
    │
    └── validateAppointmentForConflictCheck(appointment)
            ├── appointment != null
            ├── appointment.getDoctor() != null
            └── appointment.getAppointmentDateTime() != null
```

### Backward Slice Variables

| Variable | Source | Role |
|---|---|---|
| `appointment` | Method parameter | The new/updated appointment being checked |
| `currentAppointment` | Method parameter (null or self) | Controls self-exclusion during updates |
| `doctorId` | `appointment.getDoctor().getId()` | Scopes the calendar query |
| `newStart` | `appointment.getAppointmentDateTime()` | New interval start |
| `newEnd` | `calculateEndTime(appointment)` | New interval end |
| `appointment.durationMinutes` | `Appointment` field | Determines interval length |
| `existingAppointment` | Repository query result | Each existing booking to check against |
| `existingAppointment.status` | `Appointment` field | Determines if the existing booking is blocking |
| `existingStart` | `existing.getAppointmentDateTime()` | Existing interval start |
| `existingEnd` | `calculateEndTime(existing)` | Existing interval end |
| `existing.durationMinutes` | `Appointment` field | Determines existing interval length |
| `appointmentRepository` | Constructor-injected dependency | Provides doctor-scoped appointment list |

---

## Forward Slice

### Slicing Criterion

**Variable:** `Appointment.durationMinutes`  
**Location:** `Appointment.java` field / `setDurationMinutes()`

### Question

What is affected when `durationMinutes` changes?

### Dependency Chain

```
Appointment.durationMinutes
    │
    ├── setDurationMinutes(int)
    │       └── validates durationMinutes > 0  (throws IllegalArgumentException if not)
    │
    ├── getDurationMinutes()
    │       │
    │       └── AppointmentService.calculateEndTime(appointment)
    │               │
    │               └── appointment.getAppointmentDateTime().plusMinutes(durationMinutes)
    │                       │
    │                       └── newEnd / existingEnd
    │                               │
    │                               └── appointmentsOverlap(newStart, newEnd, existingStart, existingEnd)
    │                                       │
    │                                       ├── TRUE  →  AppointmentConflictException thrown
    │                                       │              └── scheduleAppointment: save() NOT called
    │                                       │              └── updateAppointment: update() NOT called
    │                                       │
    │                                       └── FALSE →  No conflict
    │                                                      └── scheduleAppointment: save() called
    │                                                      └── updateAppointment: update() called
    │
    └── schema.sql: duration_minutes INT NOT NULL DEFAULT 30
            └── (Schema artifact aligned with domain model, but NOT currently
                 wired to production persistence — no JDBC repository exists)
```

### Forward Slice Affected Statements

| Location | Statement / Method | Effect |
|---|---|---|
| `Appointment.setDurationMinutes()` | Validation guard | `IllegalArgumentException` if `<= 0` |
| `AppointmentService.calculateEndTime()` | `plusMinutes(durationMinutes)` | Determines interval endpoint |
| `AppointmentService.conflictsWith()` | Uses `existingEnd` from `calculateEndTime` | Existing appointment's interval depends on its `durationMinutes` |
| `AppointmentService.ensureNoAppointmentConflict()` | Uses `newEnd` from `calculateEndTime` | New appointment's interval depends on its `durationMinutes` |
| `AppointmentService.appointmentsOverlap()` | Compares all four endpoints | Overlap decision directly affected |
| `AppointmentService.scheduleAppointment()` | Exception or save path | Control flow outcome |
| `AppointmentService.updateAppointment()` | Exception or update path | Control flow outcome |

---

## Slice Boundaries

### Components Inside the Slice

- `Appointment.java` — `durationMinutes`, `appointmentDateTime`, `patient`, `doctor`, `status`
- `AppointmentStatus.java` — enum values used by `isBlockingStatus`
- `AppointmentService.java` — all conflict detection methods
- `AppointmentRepository.java` — `findByDoctorId()` interface contract
- `AppointmentConflictException.java` — exception raised on conflict

### Components Outside the Slice

- `Patient.java`, `Doctor.java`, `MedicalRecord.java` — not in the conflict computation path
- `PatientService`, `DoctorService`, `MedicalRecordService` — zero coupling to `AppointmentService`
- `DatabaseConfig.java`, `db.properties` — not referenced by any code in the slice
- `schema.sql` — structural alignment artifact, not part of the executable static slice (no JDBC repository maps `durationMinutes` to `duration_minutes` at runtime)

---

## Analysis Notes

1. **Static slicing** was performed by tracing data and control dependencies through the source code without executing the program.
2. **Dynamic slicing** was NOT performed. Dynamic slicing would require executing the program with specific inputs and tracing actual runtime variable values.
3. The backward and forward slices converge on the same core methods in `AppointmentService`, confirming that the conflict detection logic is cohesive and well-bounded.
