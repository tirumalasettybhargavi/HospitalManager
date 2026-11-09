package com.hospital.hospitalmanager.util;

/** Thrown when the user enters missing or invalid data. */
public class ValidationException extends HospitalException {
    public ValidationException(String message) {
        super(message);
    }
}
