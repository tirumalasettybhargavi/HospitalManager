package com.hospital.hospitalmanager.util;

/** Thrown when an ID does not match any stored record. */
public class RecordNotFoundException extends HospitalException {
    public RecordNotFoundException(String message) {
        super(message);
    }
}
