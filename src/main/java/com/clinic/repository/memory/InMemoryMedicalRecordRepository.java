package com.clinic.repository.memory;

import com.clinic.model.MedicalRecord;
import com.clinic.repository.MedicalRecordRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Console demo in-memory persistence. Data is lost when the application exits.
 */
public class InMemoryMedicalRecordRepository implements MedicalRecordRepository {

    private final Map<Integer, MedicalRecord> records = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public MedicalRecord save(MedicalRecord record) {
        assignIdIfNeeded(record);
        records.put(record.getId(), record);
        return record;
    }

    @Override
    public Optional<MedicalRecord> findById(Integer id) {
        return Optional.ofNullable(records.get(id));
    }

    @Override
    public List<MedicalRecord> findAll() {
        return new ArrayList<>(records.values());
    }

    @Override
    public MedicalRecord update(MedicalRecord record) {
        assignIdIfNeeded(record);
        records.put(record.getId(), record);
        return record;
    }

    @Override
    public boolean delete(Integer id) {
        return records.remove(id) != null;
    }

    @Override
    public List<MedicalRecord> findByPatientId(int patientId) {
        return records.values().stream()
                .filter(record -> record.getPatient().getId() == patientId)
                .toList();
    }

    private void assignIdIfNeeded(MedicalRecord record) {
        if (record.getId() == 0) {
            record.setId(nextId++);
        } else {
            nextId = Math.max(nextId, record.getId() + 1);
        }
    }
}
