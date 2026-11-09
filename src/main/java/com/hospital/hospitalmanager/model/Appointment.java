package com.hospital.hospitalmanager.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {
    private final String id;
    private final String patientId;
    private final String doctorId;
    private final LocalDate date;
    private final LocalTime time;
    private AppointmentStatus status;

    public Appointment(String id, String patientId, String doctorId,
                       LocalDate date, LocalTime time, AppointmentStatus status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public AppointmentStatus getStatus() { return status; }

    public void setStatus(AppointmentStatus status) { this.status = status; }

    public boolean isActive() {
        return status != AppointmentStatus.CANCELLED;
    }
}
