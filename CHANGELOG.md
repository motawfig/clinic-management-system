# Changelog

## [SMR-001] — Appointment Conflict Detection — 2026-09-16

### Added
- `AppointmentConflictException.java` — unchecked exception signaling appointment calendar collisions
- `InMemoryAppointmentRepository.java` — in-memory test double implementing `AppointmentRepository` (`src/test/java`)
- `AppointmentServiceTest.java` — 12 automated tests covering scheduling, conflicts, updates, status rules, and boundary conditions

### Modified
- `Appointment.java` — added `durationMinutes` field (default 30) with setter validation (`> 0`), parameterized constructor updated
- `AppointmentService.java` — added conflict detection in `scheduleAppointment()` and `updateAppointment()`, refactored into helper methods: `ensureNoAppointmentConflictForScheduling`, `ensureNoAppointmentConflictForUpdate`, `ensureNoAppointmentConflict`, `validateAppointmentForConflictCheck`, `isCurrentAppointment`, `conflictsWith`, `calculateEndTime`, `isBlockingStatus`, `appointmentsOverlap`
- `schema.sql` — added `duration_minutes INT NOT NULL DEFAULT 30` to the `appointments` table
- `AppointmentTest.java` — added 3 duration-related unit tests

### Refactoring (Phase 7)
- Decomposed monolithic conflict validation logic in `AppointmentService` into cohesive single-responsibility private methods
- No behavioral changes — 42/42 tests pass before and after refactoring

### Documentation
- Bilingual Arabic/English JavaDoc comments added to all modified source files
- Repository documentation created: README.md, CHANGELOG.md, and docs/ directory
- Phase reports: Maintenance Classification, Code Translation, Program Slicing, Reverse Engineering

### Test Results
```
Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
