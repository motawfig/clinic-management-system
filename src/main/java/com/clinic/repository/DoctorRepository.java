package com.clinic.repository;

import com.clinic.model.Doctor;

import java.util.List;

/**
 * Repository interface for {@link Doctor} entities.
 */
public interface DoctorRepository extends GenericRepository<Doctor, Integer> {

    /**
     * Finds doctors by their specialization.
     *
     * @param specialization the specialization to search for
     * @return matching doctors
     */
    List<Doctor> findBySpecialization(String specialization);
}
