package com.hospital.hospitalmanager.model;

public class Doctor extends Person {
    private String specialization;
    private String availability;

    public Doctor(String id, String name, String contact, String specialization, String availability) {
        super(id, name, contact);
        this.specialization = specialization;
        this.availability = availability;
    }

    public String getSpecialization() { return specialization; }
    public String getAvailability() { return availability; }

    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setAvailability(String availability) { this.availability = availability; }

    @Override
    public String getRole() { return "Doctor"; }

    /** Shown in drop-down lists. */
    @Override
    public String toString() {
        return getId() + " - " + getName() + " (" + specialization + ")";
    }
}
