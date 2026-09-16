package com.clinic.exception;

/**
 * Indicates that an appointment conflicts with an existing appointment.
 * <p>
 * يستخدمها AppointmentService كإشارة أعمال واضحة قبل تنفيذ الحفظ أو التحديث.
 * This exception marks a scheduling rule violation, not a database failure.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
