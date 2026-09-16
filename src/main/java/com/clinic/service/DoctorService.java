package com.clinic.service;

import com.clinic.model.Doctor;
import com.clinic.repository.DoctorRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Doctor} entities.
 */
public class DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * Creates a DoctorService with the given repository.
     *
     * @param doctorRepository the doctor repository
     */
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    /**
     * Registers a new doctor.
     *
     * @param doctor the doctor to register
     * @return the saved doctor
     */
    public Doctor registerDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    /**
     * Finds a doctor by their identifier.
     *
     * @param id the doctor's identifier
     * @return an Optional containing the doctor if found
     */
    public Optional<Doctor> findDoctorById(int id) {
        return doctorRepository.findById(id);
    }

    /**
     * Returns all registered doctors.
     *
     * @return list of all doctors
     */
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    /**
     * Updates an existing doctor's information.
     *
     * @param doctor the doctor with updated details
     * @return the updated doctor
     */
    public Doctor updateDoctor(Doctor doctor) {
        return doctorRepository.update(doctor);
    }

    /**
     * Deletes a doctor by their identifier.
     *
     * @param id the doctor's identifier
     * @return true if deleted successfully
     */
    public boolean deleteDoctor(int id) {
        return doctorRepository.delete(id);
    }

    /**
     * Searches doctors by specialization.
     *
     * @param specialization the specialization to search
     * @return matching doctors
     */
    public List<Doctor> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }
}
