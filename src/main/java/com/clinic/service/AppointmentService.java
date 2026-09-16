package com.clinic.service;

import com.clinic.model.Appointment;
import com.clinic.repository.AppointmentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Appointment} entities.
 * <p>
 * Appointment conflict detection will be implemented in a later phase.
 */
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Creates an AppointmentService with the given repository.
     *
     * @param appointmentRepository the appointment repository
     */
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Schedules a new appointment.
     *
     * @param appointment the appointment to schedule
     * @return the saved appointment
     */
    public Appointment scheduleAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    /**
     * Finds an appointment by its identifier.
     *
     * @param id the appointment's identifier
     * @return an Optional containing the appointment if found
     */
    public Optional<Appointment> findAppointmentById(int id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Returns all appointments.
     *
     * @return list of all appointments
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Updates an existing appointment.
     *
     * @param appointment the appointment with updated details
     * @return the updated appointment
     */
    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.update(appointment);
    }

    /**
     * Cancels an appointment by its identifier.
     *
     * @param id the appointment's identifier
     * @return true if deleted successfully
     */
    public boolean cancelAppointment(int id) {
        return appointmentRepository.delete(id);
    }

    /**
     * Finds all appointments for a given patient.
     *
     * @param patientId the patient's identifier
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Finds all appointments for a given doctor.
     *
     * @param doctorId the doctor's identifier
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    /**
     * Finds all appointments on a given date.
     *
     * @param date the date to search
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }
}
