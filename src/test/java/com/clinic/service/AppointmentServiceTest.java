package com.clinic.service;

import com.clinic.exception.AppointmentConflictException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.model.Gender;
import com.clinic.model.Patient;
import com.clinic.repository.InMemoryAppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AppointmentService}.
 */
class AppointmentServiceTest {

    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 3, 15, 10, 0);

    @Test
    @DisplayName("scheduleAppointment saves appointment when no existing conflict")
    void scheduleAppointmentWithNoExistingConflictSavesAppointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        Appointment appointment = appointment(1, doctor(1), BASE_TIME, 30, AppointmentStatus.SCHEDULED);

        Appointment saved = service.scheduleAppointment(appointment);

        assertEquals(appointment, saved);
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.findById(1).isPresent());
    }

    @Test
    @DisplayName("scheduleAppointment rejects same doctor overlapping blocking appointment")
    void scheduleAppointmentRejectsSameDoctorOverlappingAppointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 60, AppointmentStatus.SCHEDULED));
        Appointment rejected = appointment(2, doctor(1), BASE_TIME.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);

        assertThrows(AppointmentConflictException.class, () -> service.scheduleAppointment(rejected));

        assertTrue(repository.findById(2).isEmpty());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("scheduleAppointment allows overlapping appointment for different doctor")
    void scheduleAppointmentAllowsOverlappingAppointmentForDifferentDoctor() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 60, AppointmentStatus.SCHEDULED));
        Appointment appointment = appointment(2, doctor(2), BASE_TIME.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);

        Appointment saved = service.scheduleAppointment(appointment);

        assertEquals(appointment, saved);
        assertEquals(2, repository.findAll().size());
        assertTrue(repository.findById(2).isPresent());
    }

    @Test
    @DisplayName("scheduleAppointment allows same doctor non-overlapping appointment")
    void scheduleAppointmentAllowsSameDoctorNonOverlappingAppointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 30, AppointmentStatus.SCHEDULED));
        Appointment appointment = appointment(2, doctor(1), BASE_TIME.plusMinutes(45), 30, AppointmentStatus.SCHEDULED);

        Appointment saved = service.scheduleAppointment(appointment);

        assertEquals(appointment, saved);
        assertEquals(2, repository.findAll().size());
    }

    @Test
    @DisplayName("scheduleAppointment allows boundary touching appointment")
    void scheduleAppointmentAllowsBoundaryTouchingAppointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 30, AppointmentStatus.SCHEDULED));
        Appointment appointment = appointment(2, doctor(1), BASE_TIME.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);

        Appointment saved = service.scheduleAppointment(appointment);

        assertEquals(appointment, saved);
        assertEquals(2, repository.findAll().size());
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"COMPLETED", "CANCELLED", "NO_SHOW"})
    @DisplayName("scheduleAppointment ignores overlapping non-blocking appointment statuses")
    void scheduleAppointmentIgnoresOverlappingNonBlockingStatuses(AppointmentStatus status) {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 60, status));
        Appointment appointment = appointment(2, doctor(1), BASE_TIME.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);

        Appointment saved = service.scheduleAppointment(appointment);

        assertEquals(appointment, saved);
        assertEquals(2, repository.findAll().size());
    }

    @ParameterizedTest
    @EnumSource(value = AppointmentStatus.class, names = {"SCHEDULED", "CONFIRMED"})
    @DisplayName("scheduleAppointment rejects overlapping blocking appointment statuses")
    void scheduleAppointmentRejectsOverlappingBlockingStatuses(AppointmentStatus status) {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 60, status));
        Appointment rejected = appointment(2, doctor(1), BASE_TIME.plusMinutes(30), 30, AppointmentStatus.SCHEDULED);

        assertThrows(AppointmentConflictException.class, () -> service.scheduleAppointment(rejected));

        assertTrue(repository.findById(2).isEmpty());
        assertEquals(1, repository.findAll().size());
    }

    @Test
    @DisplayName("updateAppointment excludes the appointment being updated")
    void updateAppointmentExcludesCurrentAppointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 30, AppointmentStatus.SCHEDULED));
        Appointment update = appointment(1, doctor(1), BASE_TIME, 45, AppointmentStatus.CONFIRMED);

        Appointment updated = service.updateAppointment(update);

        assertEquals(update, updated);
        assertEquals(45, repository.findById(1).orElseThrow().getDurationMinutes());
        assertEquals(AppointmentStatus.CONFIRMED, repository.findById(1).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("updateAppointment rejects update that creates conflict")
    void updateAppointmentRejectsUpdateThatCreatesConflict() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        AppointmentService service = new AppointmentService(repository);
        repository.save(appointment(1, doctor(1), BASE_TIME, 30, AppointmentStatus.SCHEDULED));
        repository.save(appointment(2, doctor(1), BASE_TIME.plusMinutes(60), 30, AppointmentStatus.SCHEDULED));
        Appointment conflictingUpdate = appointment(2, doctor(1), BASE_TIME.plusMinutes(15), 30, AppointmentStatus.SCHEDULED);

        assertThrows(AppointmentConflictException.class, () -> service.updateAppointment(conflictingUpdate));

        Appointment stored = repository.findById(2).orElseThrow();
        assertEquals(BASE_TIME.plusMinutes(60), stored.getAppointmentDateTime());
        assertEquals(2, repository.findAll().size());
    }

    private Appointment appointment(int id, Doctor doctor, LocalDateTime start,
                                    int durationMinutes, AppointmentStatus status) {
        return new Appointment(id, patient(id), doctor, start, durationMinutes, status, null);
    }

    private Patient patient(int id) {
        return new Patient(id, "Patient " + id, LocalDate.of(1990, 1, 1),
                Gender.MALE, "555-0100", "patient" + id + "@example.com", "123 Main St");
    }

    private Doctor doctor(int id) {
        return new Doctor(id, "Doctor " + id, "General Medicine", "555-0200",
                "doctor" + id + "@clinic.com");
    }
}
