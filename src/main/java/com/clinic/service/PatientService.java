package com.clinic.service;

import com.clinic.model.Patient;
import com.clinic.repository.PatientRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link Patient} entities.
 */
public class PatientService {

    private final PatientRepository patientRepository;

    /**
     * Creates a PatientService with the given repository.
     *
     * @param patientRepository the patient repository
     */
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Registers a new patient.
     *
     * @param patient the patient to register
     * @return the saved patient
     */
    public Patient registerPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    /**
     * Finds a patient by their identifier.
     *
     * @param id the patient's identifier
     * @return an Optional containing the patient if found
     */
    public Optional<Patient> findPatientById(int id) {
        return patientRepository.findById(id);
    }

    /**
     * Returns all registered patients.
     *
     * @return list of all patients
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Updates an existing patient's information.
     *
     * @param patient the patient with updated details
     * @return the updated patient
     */
    public Patient updatePatient(Patient patient) {
        return patientRepository.update(patient);
    }

    /**
     * Deletes a patient by their identifier.
     *
     * @param id the patient's identifier
     * @return true if deleted successfully
     */
    public boolean deletePatient(int id) {
        return patientRepository.delete(id);
    }

    /**
     * Searches patients by name.
     *
     * @param name the search text
     * @return matching patients
     */
    public List<Patient> searchByName(String name) {
        return patientRepository.findByName(name);
    }
}
