package com.clinic.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a patient in the clinic management system.
 */
public class Patient {

    private int id;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String email;
    private String address;

    /**
     * Default constructor.
     */
    public Patient() {
    }

    /**
     * Creates a new Patient with the specified details.
     *
     * @param id          unique identifier
     * @param fullName    patient's full name (must not be null or blank)
     * @param dateOfBirth patient's date of birth
     * @param gender      patient's gender
     * @param phone       contact phone number
     * @param email       email address
     * @param address     residential address
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Patient(int id, String fullName, LocalDate dateOfBirth, Gender gender,
                   String phone, String email, String address) {
        setFullName(fullName);
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the patient's full name.
     *
     * @param fullName the full name; must not be null or blank
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be null or blank");
        }
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return id == patient.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender=" + gender +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
