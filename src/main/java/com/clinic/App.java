package com.clinic;

import com.clinic.exception.AppointmentConflictException;
import com.clinic.model.Appointment;
import com.clinic.model.AppointmentStatus;
import com.clinic.model.Doctor;
import com.clinic.model.Gender;
import com.clinic.model.MedicalRecord;
import com.clinic.model.Patient;
import com.clinic.repository.memory.InMemoryAppointmentRepository;
import com.clinic.repository.memory.InMemoryDoctorRepository;
import com.clinic.repository.memory.InMemoryMedicalRecordRepository;
import com.clinic.repository.memory.InMemoryPatientRepository;
import com.clinic.service.AppointmentService;
import com.clinic.service.DoctorService;
import com.clinic.service.MedicalRecordService;
import com.clinic.service.PatientService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive console entry point for demonstrating the clinic domain and SMR-001.
 */
public class App {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner scanner = new Scanner(System.in);
    private final PatientService patientService = new PatientService(new InMemoryPatientRepository());
    private final DoctorService doctorService = new DoctorService(new InMemoryDoctorRepository());
    private final AppointmentService appointmentService = new AppointmentService(new InMemoryAppointmentRepository());
    private final MedicalRecordService medicalRecordService = new MedicalRecordService(new InMemoryMedicalRecordRepository());

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        new App().run();
    }

    private void run() {
        seedDemoData();
        boolean running = true;

        while (running) {
            showMenu();
            Integer option = readInt("Choose option: ");
            if (option == null) {
                continue;
            }

            try {
                switch (option) {
                    case 1 -> addPatient();
                    case 2 -> addDoctor();
                    case 3 -> scheduleAppointment();
                    case 4 -> updateAppointment();
                    case 5 -> cancelAppointment();
                    case 6 -> listAppointments();
                    case 7 -> listAppointmentsByDoctor();
                    case 8 -> addMedicalRecord();
                    case 9 -> viewDemoData();
                    case 10 -> running = false;
                    default -> System.out.println("Invalid option. Please choose a number from 1 to 10.");
                }
            } catch (IllegalArgumentException exception) {
                System.out.println("Unable to complete the operation: " + exception.getMessage());
            }
        }

        System.out.println("Goodbye.");
    }

    private void showMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("CLINIC MANAGEMENT SYSTEM");
        System.out.println("========================================");
        System.out.println("1. Add Patient");
        System.out.println("2. Add Doctor");
        System.out.println("3. Schedule Appointment");
        System.out.println("4. Update Appointment");
        System.out.println("5. Cancel Appointment");
        System.out.println("6. View All Appointments");
        System.out.println("7. View Appointments by Doctor");
        System.out.println("8. Add Medical Record");
        System.out.println("9. View Demo Data");
        System.out.println("10. Exit");
    }

    private void addPatient() {
        String name = readRequiredText("Patient name: ");
        if (name == null) {
            return;
        }
        String dateOfBirthInput = readLine("Date of birth (yyyy-MM-dd, blank to skip): ");
        LocalDate dateOfBirth = parseOptionalDate(dateOfBirthInput);
        if (!dateOfBirthInput.isBlank() && dateOfBirth == null) {
            return;
        }
        String genderInput = readLine("Gender (MALE, FEMALE, OTHER; blank to skip): ");
        Gender gender = parseOptionalGender(genderInput);
        if (!genderInput.isBlank() && gender == null) {
            return;
        }
        Patient patient = new Patient(0, name, dateOfBirth, gender,
                readOptionalText("Phone (blank to skip): "),
                readOptionalText("Email (blank to skip): "),
                readOptionalText("Address (blank to skip): "));
        Patient saved = patientService.registerPatient(patient);
        System.out.println("Patient added with ID " + saved.getId() + ".");
    }

    private void addDoctor() {
        String name = readRequiredText("Doctor name: ");
        if (name == null) {
            return;
        }
        String specialization = readRequiredText("Specialization: ");
        if (specialization == null) {
            return;
        }
        Doctor doctor = new Doctor(0, name, specialization,
                readOptionalText("Phone (blank to skip): "),
                readOptionalText("Email (blank to skip): "));
        Doctor saved = doctorService.registerDoctor(doctor);
        System.out.println("Doctor added with ID " + saved.getId() + ".");
    }

    private void scheduleAppointment() {
        Patient patient = findPatientByPrompt();
        if (patient == null) {
            return;
        }
        Doctor doctor = findDoctorByPrompt();
        if (doctor == null) {
            return;
        }
        LocalDateTime appointmentDateTime = readDateTime("Appointment date and time (yyyy-MM-dd HH:mm): ");
        if (appointmentDateTime == null) {
            return;
        }
        Integer durationMinutes = readPositiveInt("Duration in minutes: ");
        if (durationMinutes == null) {
            return;
        }
        AppointmentStatus status = readStatus("Status (SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW): ");
        if (status == null) {
            return;
        }

        Appointment appointment = new Appointment(0, patient, doctor, appointmentDateTime,
                durationMinutes, status, null);
        try {
            Appointment saved = appointmentService.scheduleAppointment(appointment);
            System.out.println("Appointment scheduled with ID " + saved.getId() + ".");
        } catch (AppointmentConflictException exception) {
            System.out.println("Appointment conflict detected. The selected doctor already has an overlapping appointment.");
            System.out.println("يوجد تعارض في الموعد، الطبيب لديه موعد متداخل في نفس الفترة.");
        }
    }

    private void updateAppointment() {
        Integer appointmentId = readInt("Appointment ID: ");
        if (appointmentId == null) {
            return;
        }
        Appointment existing = appointmentService.findAppointmentById(appointmentId).orElse(null);
        if (existing == null) {
            System.out.println("Appointment not found.");
            return;
        }

        LocalDateTime dateTime = readOptionalDateTime(
                "New date/time (yyyy-MM-dd HH:mm, blank keeps " + formatDateTime(existing.getAppointmentDateTime()) + "): ",
                existing.getAppointmentDateTime());
        if (dateTime == null) {
            return;
        }
        Integer duration = readOptionalPositiveInt(
                "New duration in minutes (blank keeps " + existing.getDurationMinutes() + "): ",
                existing.getDurationMinutes());
        if (duration == null) {
            return;
        }
        AppointmentStatus status = readOptionalStatus(
                "New status (blank keeps " + existing.getStatus() + "): ", existing.getStatus());
        if (status == null) {
            return;
        }

        // Use a separate object so a rejected update cannot alter the stored appointment by reference.
        Appointment updatedAppointment = new Appointment(existing.getId(), existing.getPatient(), existing.getDoctor(),
                dateTime, duration, status, existing.getNotes());
        try {
            appointmentService.updateAppointment(updatedAppointment);
            System.out.println("Appointment updated.");
        } catch (AppointmentConflictException exception) {
            System.out.println("Appointment conflict detected. The selected doctor already has an overlapping appointment.");
            System.out.println("يوجد تعارض في الموعد، الطبيب لديه موعد متداخل في نفس الفترة.");
        }
    }

    private void cancelAppointment() {
        Integer appointmentId = readInt("Appointment ID: ");
        if (appointmentId == null) {
            return;
        }
        if (appointmentService.cancelAppointment(appointmentId)) {
            System.out.println("Appointment cancelled.");
        } else {
            System.out.println("Appointment not found.");
        }
    }

    private void listAppointments() {
        displayAppointments(appointmentService.getAllAppointments());
    }

    private void listAppointmentsByDoctor() {
        Doctor doctor = findDoctorByPrompt();
        if (doctor != null) {
            displayAppointments(appointmentService.getAppointmentsByDoctor(doctor.getId()));
        }
    }

    private void addMedicalRecord() {
        Patient patient = findPatientByPrompt();
        if (patient == null) {
            return;
        }
        String diagnosis = readRequiredText("Diagnosis: ");
        if (diagnosis == null) {
            return;
        }
        MedicalRecord record = new MedicalRecord(0, patient, diagnosis,
                readOptionalText("Treatment (blank to skip): "),
                readOptionalText("Notes (blank to skip): "), LocalDateTime.now());
        MedicalRecord saved = medicalRecordService.createRecord(record);
        System.out.println("Medical record added with ID " + saved.getId() + ".");
    }

    private void viewDemoData() {
        System.out.println("Patients:");
        patientService.getAllPatients().forEach(patient ->
                System.out.println("  " + patient.getId() + " - " + patient.getFullName()));
        System.out.println("Doctors:");
        doctorService.getAllDoctors().forEach(doctor ->
                System.out.println("  " + doctor.getId() + " - " + doctor.getFullName()
                        + " (" + doctor.getSpecialization() + ")"));
        System.out.println("Appointments:");
        displayAppointments(appointmentService.getAllAppointments());
    }

    private void seedDemoData() {
        patientService.registerPatient(new Patient(0, "Ahmed Ali", null, null, null, null, null));
        patientService.registerPatient(new Patient(0, "Sara Mohammed", null, null, null, null, null));
        doctorService.registerDoctor(new Doctor(0, "Dr. Khaled", "Cardiology", null, null));
        doctorService.registerDoctor(new Doctor(0, "Dr. Amal", "General Medicine", null, null));
        System.out.println("Demo data loaded: 2 patients and 2 doctors.");
    }

    private Patient findPatientByPrompt() {
        Integer patientId = readInt("Patient ID: ");
        if (patientId == null) {
            return null;
        }
        Patient patient = patientService.findPatientById(patientId).orElse(null);
        if (patient == null) {
            System.out.println("Patient not found.");
        }
        return patient;
    }

    private Doctor findDoctorByPrompt() {
        Integer doctorId = readInt("Doctor ID: ");
        if (doctorId == null) {
            return null;
        }
        Doctor doctor = doctorService.findDoctorById(doctorId).orElse(null);
        if (doctor == null) {
            System.out.println("Doctor not found.");
        }
        return doctor;
    }

    private void displayAppointments(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }
        System.out.printf("%-4s %-20s %-20s %-18s %-10s %-12s%n",
                "ID", "Patient", "Doctor", "Date/Time", "Duration", "Status");
        System.out.println("--------------------------------------------------------------------------------------");
        for (Appointment appointment : appointments) {
            System.out.printf("%-4d %-20s %-20s %-18s %-10d %-12s%n",
                    appointment.getId(),
                    appointment.getPatient().getFullName(),
                    appointment.getDoctor().getFullName(),
                    formatDateTime(appointment.getAppointmentDateTime()),
                    appointment.getDurationMinutes(),
                    appointment.getStatus());
        }
    }

    private Integer readInt(String prompt) {
        String input = readLine(prompt);
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private Integer readPositiveInt(String prompt) {
        Integer value = readInt(prompt);
        if (value != null && value <= 0) {
            System.out.println("Duration must be greater than zero.");
            return null;
        }
        return value;
    }

    private Integer readOptionalPositiveInt(String prompt, int currentValue) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            return currentValue;
        }
        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                System.out.println("Duration must be greater than zero.");
                return null;
            }
            return value;
        } catch (NumberFormatException exception) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        String input = readLine(prompt);
        try {
            return LocalDateTime.parse(input, DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date/time. Use format yyyy-MM-dd HH:mm.");
            return null;
        }
    }

    private LocalDateTime readOptionalDateTime(String prompt, LocalDateTime currentValue) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            return currentValue;
        }
        try {
            return LocalDateTime.parse(input, DATE_TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date/time. Use format yyyy-MM-dd HH:mm.");
            return null;
        }
    }

    private LocalDate parseOptionalDate(String input) {
        if (input.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(input, DATE_FORMAT);
        } catch (DateTimeParseException exception) {
            System.out.println("Invalid date. Use format yyyy-MM-dd.");
            return null;
        }
    }

    private Gender parseOptionalGender(String input) {
        if (input.isBlank()) {
            return null;
        }
        try {
            return Gender.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid gender. Use MALE, FEMALE, or OTHER.");
            return null;
        }
    }

    private AppointmentStatus readStatus(String prompt) {
        String input = readLine(prompt);
        try {
            return AppointmentStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid status. Use SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, or NO_SHOW.");
            return null;
        }
    }

    private AppointmentStatus readOptionalStatus(String prompt, AppointmentStatus currentValue) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            return currentValue;
        }
        try {
            return AppointmentStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException exception) {
            System.out.println("Invalid status. Use SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, or NO_SHOW.");
            return null;
        }
    }

    private String readRequiredText(String prompt) {
        String input = readLine(prompt);
        if (input.isBlank()) {
            System.out.println("This field is required.");
            return null;
        }
        return input;
    }

    private String readOptionalText(String prompt) {
        String input = readLine(prompt);
        return input.isBlank() ? null : input;
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMAT);
    }
}
