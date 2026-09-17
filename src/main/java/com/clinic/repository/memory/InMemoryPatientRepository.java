package com.clinic.repository.memory;

import com.clinic.model.Patient;
import com.clinic.repository.PatientRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Console demo in-memory persistence. Data is lost when the application exits.
 */
public class InMemoryPatientRepository implements PatientRepository {

    private final Map<Integer, Patient> patients = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Patient save(Patient patient) {
        assignIdIfNeeded(patient);
        patients.put(patient.getId(), patient);
        return patient;
    }

    @Override
    public Optional<Patient> findById(Integer id) {
        return Optional.ofNullable(patients.get(id));
    }

    @Override
    public List<Patient> findAll() {
        return new ArrayList<>(patients.values());
    }

    @Override
    public Patient update(Patient patient) {
        assignIdIfNeeded(patient);
        patients.put(patient.getId(), patient);
        return patient;
    }

    @Override
    public boolean delete(Integer id) {
        return patients.remove(id) != null;
    }

    @Override
    public List<Patient> findByName(String name) {
        String searchText = name == null ? "" : name.toLowerCase();
        return patients.values().stream()
                .filter(patient -> patient.getFullName().toLowerCase().contains(searchText))
                .toList();
    }

    private void assignIdIfNeeded(Patient patient) {
        if (patient.getId() == 0) {
            patient.setId(nextId++);
        } else {
            nextId = Math.max(nextId, patient.getId() + 1);
        }
    }
}
