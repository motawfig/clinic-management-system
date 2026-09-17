package com.clinic.repository.memory;

import com.clinic.model.Doctor;
import com.clinic.repository.DoctorRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Console demo in-memory persistence. Data is lost when the application exits.
 */
public class InMemoryDoctorRepository implements DoctorRepository {

    private final Map<Integer, Doctor> doctors = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Doctor save(Doctor doctor) {
        assignIdIfNeeded(doctor);
        doctors.put(doctor.getId(), doctor);
        return doctor;
    }

    @Override
    public Optional<Doctor> findById(Integer id) {
        return Optional.ofNullable(doctors.get(id));
    }

    @Override
    public List<Doctor> findAll() {
        return new ArrayList<>(doctors.values());
    }

    @Override
    public Doctor update(Doctor doctor) {
        assignIdIfNeeded(doctor);
        doctors.put(doctor.getId(), doctor);
        return doctor;
    }

    @Override
    public boolean delete(Integer id) {
        return doctors.remove(id) != null;
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) {
        String searchText = specialization == null ? "" : specialization.toLowerCase();
        return doctors.values().stream()
                .filter(doctor -> doctor.getSpecialization() != null
                        && doctor.getSpecialization().toLowerCase().contains(searchText))
                .toList();
    }

    private void assignIdIfNeeded(Doctor doctor) {
        if (doctor.getId() == 0) {
            doctor.setId(nextId++);
        } else {
            nextId = Math.max(nextId, doctor.getId() + 1);
        }
    }
}
