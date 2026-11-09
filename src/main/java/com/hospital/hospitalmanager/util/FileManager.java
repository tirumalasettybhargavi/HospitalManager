package com.hospital.hospitalmanager.util;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.model.AppointmentStatus;
import com.hospital.hospitalmanager.model.Doctor;
import com.hospital.hospitalmanager.model.Patient;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All file handling lives here. Each record is one line of text in a file inside
 * the "data" folder, with fields separated by the | character.
 * Missing files are treated as empty; damaged lines are skipped and reported as warnings.
 */
public class FileManager {
    private final Path dir;
    private final Path patientFile;
    private final Path doctorFile;
    private final Path appointmentFile;
    private final List<String> warnings = new ArrayList<>();

    public FileManager() {
        this(Paths.get("data"));
    }

    public FileManager(Path dir) {
        this.dir = dir;
        this.patientFile = dir.resolve("patients.txt");
        this.doctorFile = dir.resolve("doctors.txt");
        this.appointmentFile = dir.resolve("appointments.txt");
    }

    public Path getDataDirectory() { return dir.toAbsolutePath(); }
    public List<String> getWarnings() { return new ArrayList<>(warnings); }
    public void clearWarnings() { warnings.clear(); }

    // ====================== SAVE ======================

    public void savePatients(List<Patient> patients) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Patient p : patients) {
            lines.add(join(p.getId(), p.getName(), String.valueOf(p.getAge()), p.getGender(),
                    p.getContact(), p.getAddress(), p.getMedicalHistory()));
        }
        writeLines(patientFile, lines);
    }

    public void saveDoctors(List<Doctor> doctors) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Doctor d : doctors) {
            lines.add(join(d.getId(), d.getName(), d.getSpecialization(),
                    d.getContact(), d.getAvailability()));
        }
        writeLines(doctorFile, lines);
    }

    public void saveAppointments(List<Appointment> appointments) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Appointment a : appointments) {
            lines.add(join(a.getId(), a.getPatientId(), a.getDoctorId(),
                    a.getDate().toString(), a.getTime().toString(), a.getStatus().name()));
        }
        writeLines(appointmentFile, lines);
    }

    // ====================== LOAD ======================

    public List<Patient> loadPatients() throws StorageException {
        List<Patient> result = new ArrayList<>();
        int lineNo = 0;
        for (String line : readLines(patientFile)) {
            lineNo++;
            if (line.isBlank()) continue;
            try {
                String[] f = split(line, 7);
                result.add(new Patient(f[0], f[1], f[4], Integer.parseInt(f[2]), f[3], f[5], f[6]));
            } catch (IllegalArgumentException e) {
                skip(patientFile, lineNo, e);
            }
        }
        return result;
    }

    public List<Doctor> loadDoctors() throws StorageException {
        List<Doctor> result = new ArrayList<>();
        int lineNo = 0;
        for (String line : readLines(doctorFile)) {
            lineNo++;
            if (line.isBlank()) continue;
            try {
                String[] f = split(line, 5);
                result.add(new Doctor(f[0], f[1], f[3], f[2], f[4]));
            } catch (IllegalArgumentException e) {
                skip(doctorFile, lineNo, e);
            }
        }
        return result;
    }

    public List<Appointment> loadAppointments() throws StorageException {
        List<Appointment> result = new ArrayList<>();
        int lineNo = 0;
        for (String line : readLines(appointmentFile)) {
            lineNo++;
            if (line.isBlank()) continue;
            try {
                String[] f = split(line, 6);
                result.add(new Appointment(f[0], f[1], f[2], LocalDate.parse(f[3]),
                        LocalTime.parse(f[4]), AppointmentStatus.valueOf(f[5])));
            } catch (IllegalArgumentException | DateTimeException e) {
                skip(appointmentFile, lineNo, e);
            }
        }
        return result;
    }

    // ====================== HELPERS ======================

    private void skip(Path file, int lineNo, RuntimeException e) {
        warnings.add(file.getFileName() + ", line " + lineNo + " was skipped (" + e.getMessage() + ")");
    }

    private void writeLines(Path file, List<String> lines) throws StorageException {
        try {
            Files.createDirectories(dir);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new StorageException("Could not save " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private List<String> readLines(Path file) throws StorageException {
        if (!Files.exists(file)) {
            return new ArrayList<>();          // first run: nothing saved yet
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("Could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append('|');
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    private static String[] split(String line, int expected) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != expected) {
            throw new IllegalArgumentException("expected " + expected + " fields but found " + parts.length);
        }
        if (parts[0].isBlank()) {
            throw new IllegalArgumentException("missing ID");
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i] = unescape(parts[i]);
        }
        return parts;
    }

    /** Makes text safe to store on one line: \ | and line breaks are replaced by codes. */
    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '|' -> sb.append("\\p");
                case '\n' -> sb.append("\\n");
                case '\r' -> { }
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'p' -> sb.append('|');
                    case 'n' -> sb.append('\n');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
