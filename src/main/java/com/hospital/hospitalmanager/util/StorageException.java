package com.hospital.hospitalmanager.util;

/** Thrown when a data file cannot be read or written. */
public class StorageException extends HospitalException {
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
