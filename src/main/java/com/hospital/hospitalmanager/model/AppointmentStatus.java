package com.hospital.hospitalmanager.model;

public enum AppointmentStatus {
    CONFIRMED("Confirmed"),
    PENDING("Pending"),
    CANCELLED("Cancelled");

    private final String label;

    AppointmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }

    public static AppointmentStatus fromLabel(String label) {
        for (AppointmentStatus s : values()) {
            if (s.label.equalsIgnoreCase(label)) {
                return s;
            }
        }
        return PENDING;
    }

    /** Shown in drop-down lists. */
    @Override
    public String toString() { return label; }
}
