package com.clinic.repository;

import com.clinic.model.Patient;

import java.util.List;

/**
 * Repository interface for {@link Patient} entities.
 */
public interface PatientRepository extends GenericRepository<Patient, Integer> {

    /**
     * Finds patients whose full name contains the given text (case-insensitive).
     *
     * @param name the search text
     * @return matching patients
     */
    List<Patient> findByName(String name);
}
