package com.clinic.model;

import java.util.Objects;

/**
 * Represents a doctor in the clinic management system.
 */
public class Doctor {

    private int id;
    private String fullName;
    private String specialization;
    private String phone;
    private String email;

    /**
     * Default constructor.
     */
    public Doctor() {
    }

    /**
     * Creates a new Doctor with the specified details.
     *
     * @param id             unique identifier
     * @param fullName       doctor's full name (must not be null or blank)
     * @param specialization medical specialization
     * @param phone          contact phone number
     * @param email          email address
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Doctor(int id, String fullName, String specialization, String phone, String email) {
        setFullName(fullName);
        this.id = id;
        this.specialization = specialization;
        this.phone = phone;
        this.email = email;
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
     * Sets the doctor's full name.
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

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Doctor doctor = (Doctor) o;
        return id == doctor.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
