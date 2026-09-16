# Test Evidence

## Final Regression Results

```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Command: `mvn clean test`

---

## Test Suite Breakdown

| Test Class | Tests | Scope |
|---|:---:|---|
| `AppointmentTest` | 9 | Domain model — appointment invariants, duration validation |
| `DoctorTest` | 7 | Domain model — doctor invariants, name validation |
| `MedicalRecordTest` | 6 | Domain model — medical record invariants |
| `PatientTest` | 8 | Domain model — patient invariants, name validation |
| `AppointmentServiceTest` | 12 | Service layer — conflict detection, scheduling, updates |
| **Total** | **42** | |

---

## Test Infrastructure

- **Framework:** JUnit Jupiter 5.10.2
- **Build Tool:** Maven Surefire Plugin 3.2.5
- **Test Double:** `InMemoryAppointmentRepository` (LinkedHashMap-backed, `src/test/java`)
- **Isolation:** Service tests are fully decoupled from database, network, and external dependencies

---

## Acceptance Criteria Verification

### SMR-001 Acceptance Criteria

| ID | Criterion | Status | Evidence |
|---|---|:---:|---|
| AC-01 | Appointment model supports `durationMinutes` (default 30, must be > 0) | **PASS** | `Appointment.java` — `setDurationMinutes()` validates > 0; default constructor sets 30. `AppointmentTest` includes 3 duration tests. |
| AC-02 | `AppointmentConflictException` created as unchecked exception | **PASS** | `AppointmentConflictException extends RuntimeException` in `com.clinic.exception`. |
| AC-03 | `scheduleAppointment()` detects and blocks overlapping appointments for the same doctor | **PASS** | `AppointmentService.scheduleAppointment()` calls `ensureNoAppointmentConflictForScheduling()` before `save()`. `AppointmentServiceTest` verifies conflict rejection. |
| AC-04 | Only `SCHEDULED` and `CONFIRMED` statuses block time | **PASS** | `isBlockingStatus()` returns true only for these two. `AppointmentServiceTest` uses `@ParameterizedTest` with `@EnumSource` to verify all five statuses. |
| AC-05 | `updateAppointment()` includes self-exclusion | **PASS** | `ensureNoAppointmentConflictForUpdate()` passes the current appointment for ID-based self-exclusion. `AppointmentServiceTest.updateOwnAppointmentWithoutConflict` verifies. |
| AC-06 | Automated test suite covers all conflict scenarios | **PASS** | 12 service tests cover: normal scheduling, same-doctor conflict, different-doctor allowed, boundary-touching, blocking statuses, non-blocking statuses, update conflict, update self-move, null guards. |
| AC-07 | Regression: all pre-existing tests continue to pass | **PASS** | 30 pre-existing model tests (Patient 8, Doctor 7, MedicalRecord 6, Appointment 9) all pass alongside 12 new service tests. Total: 42/42. |
| AC-08 | Boundary-touching appointments are allowed | **PASS** | `AppointmentServiceTest.scheduleAppointmentAtBoundaryNoConflict` verifies that an appointment starting exactly when another ends is permitted. |

---

## Design Verification Requirements

| ID | Requirement | Status | Notes |
|---|---|:---:|---|
| DVR-01 | Conflict check occurs before repository `save()` | **PASS** | `scheduleAppointment()` calls `ensureNoAppointmentConflictForScheduling()` before `appointmentRepository.save()`. |
| DVR-02 | Conflict check occurs before repository `update()` | **PASS** | `updateAppointment()` calls `ensureNoAppointmentConflictForUpdate()` before `appointmentRepository.update()`. |
| DVR-03 | Exception prevents persistence | **PASS** | `AppointmentConflictException` is thrown before the repository call is reached; control never reaches `save()`/`update()`. |
| DVR-04 | Doctor-scoped querying returns correct appointments | **PASS** | `InMemoryAppointmentRepository.findByDoctorId()` filters by doctor ID. Production behavior depends on a future concrete repository implementation correctly honoring `findByDoctorId`. |
| DVR-05 | Half-open interval semantics correct | **PASS** | `appointmentsOverlap()` uses strict `isBefore`/`isAfter` comparisons, confirming `[start, end)` semantics where equal boundaries do not overlap. |
| DVR-06 | Self-exclusion prevents false positives during update | **PASS** | `isCurrentAppointment()` compares IDs; when the existing appointment matches the appointment being updated, the loop skips it via `continue`. |

---

## Pre-Change vs. Post-Change Test Count

| Phase | Tests | Failures | Status |
|---|:---:|:---:|---|
| Phase 1 (Baseline) | 27 | 0 | BUILD SUCCESS |
| Phase 4 (Post-Implementation) | 42 | 0 | BUILD SUCCESS |
| Phase 7 (Post-Refactoring) | 42 | 0 | BUILD SUCCESS |
| Phase 10 (Final) | 42 | 0 | BUILD SUCCESS |
