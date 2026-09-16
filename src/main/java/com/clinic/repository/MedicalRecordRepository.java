package com.clinic.repository;

import com.clinic.model.MedicalRecord;

import java.util.List;

/**
 * Repository interface for {@link MedicalRecord} entities.
 */
public interface MedicalRecordRepository extends GenericRepository<MedicalRecord, Integer> {

    /**
     * Finds all medical records for a given patient.
     *
     * @param patientId the patient's identifier
     * @return list of medical records
     */
    List<MedicalRecord> findByPatientId(int patientId);
}
