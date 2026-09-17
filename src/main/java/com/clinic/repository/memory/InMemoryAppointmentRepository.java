package com.clinic.repository.memory;

import com.clinic.model.Appointment;
import com.clinic.repository.AppointmentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Console demo in-memory persistence. Data is lost when the application exits.
 */
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final Map<Integer, Appointment> appointments = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Appointment save(Appointment appointment) {
        assignIdIfNeeded(appointment);
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(Integer id) {
        return Optional.ofNullable(appointments.get(id));
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(appointments.values());
    }

    @Override
    public Appointment update(Appointment appointment) {
        assignIdIfNeeded(appointment);
        appointments.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public boolean delete(Integer id) {
        return appointments.remove(id) != null;
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getPatient().getId() == patientId)
                .toList();
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getDoctor().getId() == doctorId)
                .toList();
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        return appointments.values().stream()
                .filter(appointment -> appointment.getAppointmentDateTime().toLocalDate().equals(date))
                .toList();
    }

    private void assignIdIfNeeded(Appointment appointment) {
        if (appointment.getId() == 0) {
            appointment.setId(nextId++);
        } else {
            nextId = Math.max(nextId, appointment.getId() + 1);
        }
    }
}
