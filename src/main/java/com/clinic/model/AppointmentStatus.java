package com.clinic.model;

/**
 * Represents the status of a medical appointment.
 * <p>
 * في قواعد التعارض الحالية، SCHEDULED و CONFIRMED فقط يحجزان وقت الطبيب.
 * COMPLETED و CANCELLED و NO_SHOW لا تمنع حجز موعد جديد في نفس الفترة.
 */
public enum AppointmentStatus {
    SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW
}
