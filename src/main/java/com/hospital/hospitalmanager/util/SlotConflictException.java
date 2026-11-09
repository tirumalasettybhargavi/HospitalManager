package com.hospital.hospitalmanager.util;

/** Thrown when a doctor already has an appointment at the chosen date and time. */
public class SlotConflictException extends HospitalException {
    public SlotConflictException(String message) {
        super(message);
    }
}
