package com.clinic.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a scheduled appointment between a patient and a doctor.
 * <p>
 * يربط هذا الكيان بين المريض والطبيب ووقت الموعد وحالته. The duration is part of
 * the business model because conflict detection compares time intervals, not only
 * identical start times.
 */
public class Appointment {

    /**
     * المدة الافتراضية للموعد عند استخدام الباني القديم.
     * Default appointment length remains 30 minutes for backward compatibility.
     */
    private static final int DEFAULT_DURATION_MINUTES = 30;

    private int id;
    /**
     * علاقة الموعد بالمريض؛ يجب أن يكون لكل موعد مريض معروف.
     * Appointment-to-patient relationship.
     */
    private Patient patient;
    /**
     * علاقة الموعد بالطبيب؛ يستخدم الطبيب لعزل جدول المواعيد أثناء فحص التعارض.
     * Appointment-to-doctor relationship used for doctor-scoped scheduling.
     */
    private Doctor doctor;
    /**
     * وقت بداية الموعد.
     * The start timestamp forms the left side of the appointment interval.
     */
    private LocalDateTime appointmentDateTime;
    /**
     * مدة الموعد بالدقائق ويجب أن تكون أكبر من صفر.
     * Used with appointmentDateTime to build the interval [start, start + durationMinutes).
     */
    private int durationMinutes = DEFAULT_DURATION_MINUTES;
    /**
     * حالة الموعد تحدد هل يحجز وقت الطبيب أم لا.
     * AppointmentStatus is interpreted by AppointmentService for conflict blocking.
     */
    private AppointmentStatus status;
    private String notes;

    /**
     * Default constructor.
     */
    public Appointment() {
    }

    /**
     * Creates a new Appointment.
     *
     * @param id                  unique identifier
     * @param patient             the patient (must not be null)
     * @param doctor              the doctor (must not be null)
     * @param appointmentDateTime date and time of the appointment
     * @param status              current status
     * @param notes               additional notes
     * @throws IllegalArgumentException if patient or doctor is null
     */
    public Appointment(int id, Patient patient, Doctor doctor,
                       LocalDateTime appointmentDateTime, AppointmentStatus status,
                       String notes) {
        this(id, patient, doctor, appointmentDateTime, DEFAULT_DURATION_MINUTES, status, notes);
    }

    /**
     * Creates a new Appointment.
     *
     * @param id                  unique identifier
     * @param patient             the patient (must not be null)
     * @param doctor              the doctor (must not be null)
     * @param appointmentDateTime date and time of the appointment
     * @param durationMinutes     duration in minutes (must be greater than zero)
     * @param status              current status
     * @param notes               additional notes
     * @throws IllegalArgumentException if patient or doctor is null, or durationMinutes is not positive
     */
    public Appointment(int id, Patient patient, Doctor doctor,
                       LocalDateTime appointmentDateTime, int durationMinutes,
                       AppointmentStatus status, String notes) {
        setPatient(patient);
        setDoctor(doctor);
        setDurationMinutes(durationMinutes);
        this.id = id;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    /**
     * Sets the patient for this appointment.
     *
     * @param patient must not be null
     * @throws IllegalArgumentException if patient is null
     */
    public void setPatient(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("Patient must not be null");
        }
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    /**
     * Sets the doctor for this appointment.
     *
     * @param doctor must not be null
     * @throws IllegalArgumentException if doctor is null
     */
    public void setDoctor(Doctor doctor) {
        if (doctor == null) {
            throw new IllegalArgumentException("Doctor must not be null");
        }
        this.doctor = doctor;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * يحافظ على شرط سلامة المدة: لا يمكن أن تكون صفرا أو سالبة.
     *
     * @param durationMinutes appointment duration in minutes; must be positive
     * @throws IllegalArgumentException if durationMinutes is zero or negative
     */
    public void setDurationMinutes(int durationMinutes) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration minutes must be greater than zero");
        }
        this.durationMinutes = durationMinutes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getFullName() : "null") +
                ", doctor=" + (doctor != null ? doctor.getFullName() : "null") +
                ", dateTime=" + appointmentDateTime +
                ", durationMinutes=" + durationMinutes +
                ", status=" + status +
                ", notes='" + notes + '\'' +
                '}';
    }
}
