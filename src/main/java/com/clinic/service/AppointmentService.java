package com.clinic.service;

import com.clinic.exception.AppointmentConflictException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * يدير عمليات المواعيد ويحتوي قواعد SMR-001 الخاصة بمنع تعارض المواعيد.
 * Coordinates appointment operations and enforces doctor-scoped conflict detection.
 * <p>
 * تعتمد هذه الطبقة على {@link AppointmentRepository} كواجهة مجردة، لذلك تبقى قواعد
 * العمل منفصلة عن تفاصيل التخزين. Before scheduling or updating an appointment,
 * the service checks the selected doctor's calendar and raises
 * {@link AppointmentConflictException} before persistence if a blocking overlap exists.
 */
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    /**
     * Creates an AppointmentService with the given repository.
     *
     * @param appointmentRepository the appointment repository
     */
    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Schedules a new appointment after validating doctor calendar availability.
     * <p>
     * يتم فحص التعارض قبل {@code save()} حتى لا يدخل موعد غير صالح إلى طبقة التخزين.
     *
     * @param appointment the appointment to schedule
     * @return the saved appointment
     */
    public Appointment scheduleAppointment(Appointment appointment) {
        ensureNoAppointmentConflictForScheduling(appointment);

        return appointmentRepository.save(appointment);
    }

    private void ensureNoAppointmentConflictForScheduling(Appointment appointment) {
        ensureNoAppointmentConflict(appointment, null);
    }

    /**
     * يتحقق من التعارض أثناء التعديل مع استبعاد نفس الموعد الحالي.
     * The current appointment is passed so its own ID will not be treated as a conflict.
     */
    private void ensureNoAppointmentConflictForUpdate(Appointment appointment) {
        ensureNoAppointmentConflict(appointment, appointment);
    }

    /**
     * المسار المركزي لفحص التعارض: الموعد الجديد، الطبيب، وقت البداية، المدة،
     * المواعيد الحالية للطبيب، الحالة، ونقاط بداية/نهاية الفترات كلها تؤثر على القرار.
     * Central conflict path used by both scheduling and update operations.
     */
    private void ensureNoAppointmentConflict(Appointment appointment, Appointment currentAppointment) {
        validateAppointmentForConflictCheck(appointment);

        // نستخدم Doctor ID كمفتاح ثابت لعزل جدول مواعيد كل طبيب.
        // The repository must return only appointments for this doctor's calendar.
        int doctorId = appointment.getDoctor().getId();
        LocalDateTime newStart = appointment.getAppointmentDateTime();
        LocalDateTime newEnd = calculateEndTime(appointment);

        for (Appointment existingAppointment : appointmentRepository.findByDoctorId(doctorId)) {
            // أثناء التعديل، لا نقارن الموعد بنفسه حتى لا ينتج تعارض وهمي.
            // Update self-exclusion is based on appointment ID.
            if (isCurrentAppointment(existingAppointment, currentAppointment)) {
                continue;
            }
            if (conflictsWith(newStart, newEnd, existingAppointment)) {
                // يتم إيقاف الحفظ أو التحديث هنا قبل الوصول إلى طبقة التخزين.
                // A conflict diverts control away from save/update.
                throw new AppointmentConflictException("Appointment conflicts with an existing appointment");
            }
        }
    }

    /**
     * يتحقق من الحد الأدنى من البيانات المطلوبة قبل حساب الفترات الزمنية.
     * Appointment, doctor, and start time are required for reliable conflict detection.
     */
    private void validateAppointmentForConflictCheck(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment must not be null");
        }
        if (appointment.getDoctor() == null) {
            throw new IllegalArgumentException("Appointment doctor must not be null");
        }
        if (appointment.getAppointmentDateTime() == null) {
            throw new IllegalArgumentException("Appointment date and time must not be null");
        }
    }

    /**
     * يحدد هل السجل الموجود هو نفس الموعد الجاري تعديله.
     * This protects update operations from comparing an appointment against itself.
     */
    private boolean isCurrentAppointment(Appointment existingAppointment, Appointment currentAppointment) {
        return currentAppointment != null && existingAppointment.getId() == currentAppointment.getId();
    }

    /**
     * يفحص موعدا موجودا واحدا مقابل الفترة الجديدة.
     * Only blocking statuses participate in overlap detection.
     */
    private boolean conflictsWith(LocalDateTime newStart, LocalDateTime newEnd, Appointment existingAppointment) {
        if (!isBlockingStatus(existingAppointment.getStatus())) {
            return false;
        }

        LocalDateTime existingStart = existingAppointment.getAppointmentDateTime();
        LocalDateTime existingEnd = calculateEndTime(existingAppointment);

        return appointmentsOverlap(newStart, newEnd, existingStart, existingEnd);
    }

    /**
     * نحسب نهاية الموعد من وقت البداية والمدة.
     * durationMinutes flows into the end-time calculation, then into the overlap result.
     */
    private LocalDateTime calculateEndTime(Appointment appointment) {
        return appointment.getAppointmentDateTime().plusMinutes(appointment.getDurationMinutes());
    }

    /**
     * الحالات التي تمنع حجز وقت الطبيب هي SCHEDULED و CONFIRMED فقط.
     * Completed, cancelled, and no-show appointments are historical/non-blocking states.
     */
    private boolean isBlockingStatus(AppointmentStatus status) {
        return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
    }

    /**
     * يستخدم النظام فترة نصف مفتوحة: [start, start + durationMinutes).
     * Strict comparisons mean an appointment ending exactly when another starts is allowed.
     */
    private boolean appointmentsOverlap(LocalDateTime newStart, LocalDateTime newEnd,
                                        LocalDateTime existingStart, LocalDateTime existingEnd) {
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }

    /**
     * Finds an appointment by its identifier.
     *
     * @param id the appointment's identifier
     * @return an Optional containing the appointment if found
     */
    public Optional<Appointment> findAppointmentById(int id) {
        return appointmentRepository.findById(id);
    }

    /**
     * Returns all appointments.
     *
     * @return list of all appointments
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Updates an existing appointment after validating doctor calendar availability.
     * <p>
     * يتم فحص التعارض قبل {@code update()}، مع استبعاد الموعد نفسه باستخدام ID.
     *
     * @param appointment the appointment with updated details
     * @return the updated appointment
     */
    public Appointment updateAppointment(Appointment appointment) {
        ensureNoAppointmentConflictForUpdate(appointment);

        return appointmentRepository.update(appointment);
    }

    /**
     * Cancels an appointment by its identifier.
     *
     * @param id the appointment's identifier
     * @return true if deleted successfully
     */
    public boolean cancelAppointment(int id) {
        return appointmentRepository.delete(id);
    }

    /**
     * Finds all appointments for a given patient.
     *
     * @param patientId the patient's identifier
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    /**
     * Finds all appointments for a given doctor.
     *
     * @param doctorId the doctor's identifier
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    /**
     * Finds all appointments on a given date.
     *
     * @param date the date to search
     * @return list of appointments
     */
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentRepository.findByDate(date);
    }
}
