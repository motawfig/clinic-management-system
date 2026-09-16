package com.clinic.service;

import com.clinic.model.MedicalRecord;
import com.clinic.repository.MedicalRecordRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing {@link MedicalRecord} entities.
 */
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    /**
     * Creates a MedicalRecordService with the given repository.
     *
     * @param medicalRecordRepository the medical record repository
     */
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    /**
     * Creates a new medical record.
     *
     * @param record the medical record to create
     * @return the saved record
     */
    public MedicalRecord createRecord(MedicalRecord record) {
        return medicalRecordRepository.save(record);
    }

    /**
     * Finds a medical record by its identifier.
     *
     * @param id the record's identifier
     * @return an Optional containing the record if found
     */
    public Optional<MedicalRecord> findRecordById(int id) {
        return medicalRecordRepository.findById(id);
    }

    /**
     * Returns all medical records.
     *
     * @return list of all records
     */
    public List<MedicalRecord> getAllRecords() {
        return medicalRecordRepository.findAll();
    }

    /**
     * Updates an existing medical record.
     *
     * @param record the record with updated details
     * @return the updated record
     */
    public MedicalRecord updateRecord(MedicalRecord record) {
        return medicalRecordRepository.update(record);
    }

    /**
     * Deletes a medical record by its identifier.
     *
     * @param id the record's identifier
     * @return true if deleted successfully
     */
    public boolean deleteRecord(int id) {
        return medicalRecordRepository.delete(id);
    }

    /**
     * Finds all medical records for a given patient.
     *
     * @param patientId the patient's identifier
     * @return list of medical records
     */
    public List<MedicalRecord> getRecordsByPatient(int patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }
}
