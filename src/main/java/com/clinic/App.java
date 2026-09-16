package com.clinic;

import com.clinic.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clinic Management System — Application Entry Point.
 * <p>
 * Demonstrates that the system foundation has been initialized successfully.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("   Clinic Management System v1.0-SNAPSHOT");
        System.out.println("============================================");
        System.out.println();

        // Demonstrate domain model initialization
        Patient patient = new Patient(1, "Alice Johnson", LocalDate.of(1985, 3, 20),
                Gender.FEMALE, "555-0101", "alice@email.com", "100 Health Ave");
        System.out.println("Sample Patient: " + patient);

        Doctor doctor = new Doctor(1, "Dr. Robert Chen", "General Medicine",
                "555-0201", "chen@clinic.com");
        System.out.println("Sample Doctor:  " + doctor);

        Appointment appointment = new Appointment(1, patient, doctor,
                LocalDateTime.now().plusDays(7), AppointmentStatus.SCHEDULED,
                "Annual check-up");
        System.out.println("Sample Appointment: " + appointment);

        MedicalRecord record = new MedicalRecord(1, patient, "Routine Check",
                "No treatment required", "Patient in good health", LocalDateTime.now());
        System.out.println("Sample Record:  " + record);

        System.out.println();
        System.out.println("System initialized successfully.");
        System.out.println("Modules ready: model, repository, service, util");
        System.out.println("Database configuration: src/main/resources/db.properties");
        System.out.println("============================================");
    }
}
