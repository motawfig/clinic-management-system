package com.clinic.exception;

/**
 * Indicates that an appointment conflicts with an existing appointment.
 */
public class AppointmentConflictException extends RuntimeException {

    public AppointmentConflictException(String message) {
        super(message);
    }
}
