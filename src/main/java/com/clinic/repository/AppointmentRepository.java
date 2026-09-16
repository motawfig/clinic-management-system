package com.clinic.repository;

import com.clinic.model.Appointment;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for {@link Appointment} entities.
 */
public interface AppointmentRepository extends GenericRepository<Appointment, Integer> {

    /**
     * Finds all appointments for a given patient.
     *
     * @param patientId the patient's identifier
     * @return list of appointments
     */
    List<Appointment> findByPatientId(int patientId);

    /**
     * Finds all appointments for a given doctor.
     *
     * @param doctorId the doctor's identifier
     * @return list of appointments
     */
    List<Appointment> findByDoctorId(int doctorId);

    /**
     * Finds all appointments on a given date.
     *
     * @param date the date to search
     * @return list of appointments on that date
     */
    List<Appointment> findByDate(LocalDate date);
}
