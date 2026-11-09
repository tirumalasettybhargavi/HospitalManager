package com.hospital.hospitalmanager.service;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.model.AppointmentStatus;
import com.hospital.hospitalmanager.model.Doctor;
import com.hospital.hospitalmanager.model.Patient;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.util.FileManager;
import com.hospital.hospitalmanager.util.HospitalException;
import com.hospital.hospitalmanager.util.RecordNotFoundException;
import com.hospital.hospitalmanager.util.SlotConflictException;
import com.hospital.hospitalmanager.util.StorageException;
import com.hospital.hospitalmanager.util.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Business rules of the hospital: validation, unique IDs, appointment conflict checking,
 * searching and statistics. It knows nothing about JavaFX; screens only call these methods.
 */
public class HospitalManager {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,59}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^\\d{10}$");

    private final FileManager fileManager;
    private final List<Patient> patients = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<String> loadWarnings = new ArrayList<>();

    public HospitalManager(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    public FileManager getFileManager() { return fileManager; }
    public List<String> getLoadWarnings() { return new ArrayList<>(loadWarnings); }

    // ============================ LOADING ============================

    public void load() throws StorageException {
        fileManager.clearWarnings();
        List<Patient> loadedPatients = fileManager.loadPatients();
        List<Doctor> loadedDoctors = fileManager.loadDoctors();
        List<Appointment> loadedAppointments = fileManager.loadAppointments();

        loadWarnings.clear();
        loadWarnings.addAll(fileManager.getWarnings());

        patients.clear();
        Set<String> seen = new HashSet<>();
        for (Patient p : loadedPatients) {
            if (seen.add(p.getId())) patients.add(p);
            else loadWarnings.add("Duplicate patient ID " + p.getId() + " was ignored.");
        }
        doctors.clear();
        seen.clear();
        for (Doctor d : loadedDoctors) {
            if (seen.add(d.getId())) doctors.add(d);
            else loadWarnings.add("Duplicate doctor ID " + d.getId() + " was ignored.");
        }
        appointments.clear();
        seen.clear();
        for (Appointment a : loadedAppointments) {
            if (seen.add(a.getId())) appointments.add(a);
            else loadWarnings.add("Duplicate appointment ID " + a.getId() + " was ignored.");
        }
    }

    // ============================ PATIENTS ============================

    public List<Patient> getPatients() {
        List<Patient> copy = new ArrayList<>(patients);
        copy.sort(Comparator.comparing(Patient::getId));
        return copy;
    }

    public String nextPatientId() {
        return nextId("P", patients.stream().map(Patient::getId).toList());
    }

    public Patient addPatient(String name, int age, String gender, String contact,
                              String address, String history) throws HospitalException {
        validatePatient(name, age, gender, contact);
        Patient p = new Patient(nextPatientId(), name.trim(), contact.trim(), age,
                gender, clean(address), clean(history));
        patients.add(p);
        fileManager.savePatients(patients);
        return p;
    }

    public void updatePatient(String id, String name, int age, String gender, String contact,
                              String address, String history) throws HospitalException {
        Patient p = findPatientOrThrow(id);
        validatePatient(name, age, gender, contact);
        p.setName(name.trim());
        p.setAge(age);
        p.setGender(gender);
        p.setContact(contact.trim());
        p.setAddress(clean(address));
        p.setMedicalHistory(clean(history));
        fileManager.savePatients(patients);
    }

    public void deletePatient(String id) throws HospitalException {
        Patient p = findPatientOrThrow(id);
        LocalDate today = LocalDate.now();
        for (Appointment a : appointments) {
            if (a.getPatientId().equals(id) && a.isActive() && !a.getDate().isBefore(today)) {
                throw new ValidationException("Cannot delete " + p.getName()
                        + " because there are upcoming appointments. Cancel them first.");
            }
        }
        patients.remove(p);
        appointments.removeIf(a -> a.getPatientId().equals(id));
        fileManager.savePatients(patients);
        fileManager.saveAppointments(appointments);
    }

    public List<Patient> searchPatients(String query) {
        String q = normalize(query);
        List<Patient> result = new ArrayList<>();
        for (Patient p : getPatients()) {
            if (q.isEmpty() || matches(q, p.getId(), p.getName(), p.getContact())) {
                result.add(p);
            }
        }
        return result;
    }

    public Optional<Patient> findPatient(String id) {
        return patients.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    public String patientName(String id) {
        return findPatient(id).map(Patient::getName).orElse("(deleted)");
    }

    private Patient findPatientOrThrow(String id) throws RecordNotFoundException {
        return findPatient(id).orElseThrow(() -> new RecordNotFoundException("No patient found with ID " + id));
    }

    private void validatePatient(String name, int age, String gender, String contact) throws ValidationException {
        validateName(name);
        if (age < 0 || age > 120) throw new ValidationException("Age must be between 0 and 120.");
        if (gender == null || gender.isBlank()) throw new ValidationException("Please select a gender.");
        validateContact(contact);
    }

    // ============================ DOCTORS ============================

    public List<Doctor> getDoctors() {
        List<Doctor> copy = new ArrayList<>(doctors);
        copy.sort(Comparator.comparing(Doctor::getId));
        return copy;
    }

    public String nextDoctorId() {
        return nextId("D", doctors.stream().map(Doctor::getId).toList());
    }

    public Doctor addDoctor(String name, String specialization, String contact,
                            String availability) throws HospitalException {
        validateDoctor(name, specialization, contact, availability);
        Doctor d = new Doctor(nextDoctorId(), name.trim(), contact.trim(), specialization, availability);
        doctors.add(d);
        fileManager.saveDoctors(doctors);
        return d;
    }

    public void updateDoctor(String id, String name, String specialization, String contact,
                             String availability) throws HospitalException {
        Doctor d = findDoctorOrThrow(id);
        validateDoctor(name, specialization, contact, availability);
        d.setName(name.trim());
        d.setSpecialization(specialization);
        d.setContact(contact.trim());
        d.setAvailability(availability);
        fileManager.saveDoctors(doctors);
    }

    public void deleteDoctor(String id) throws HospitalException {
        Doctor d = findDoctorOrThrow(id);
        LocalDate today = LocalDate.now();
        for (Appointment a : appointments) {
            if (a.getDoctorId().equals(id) && a.isActive() && !a.getDate().isBefore(today)) {
                throw new ValidationException("Cannot delete " + d.getName()
                        + " because there are upcoming appointments. Cancel them first.");
            }
        }
        doctors.remove(d);
        appointments.removeIf(a -> a.getDoctorId().equals(id));
        fileManager.saveDoctors(doctors);
        fileManager.saveAppointments(appointments);
    }

    public List<Doctor> searchDoctors(String query) {
        String q = normalize(query);
        List<Doctor> result = new ArrayList<>();
        for (Doctor d : getDoctors()) {
            if (q.isEmpty() || matches(q, d.getId(), d.getName(), d.getSpecialization())) {
                result.add(d);
            }
        }
        return result;
    }

    public Optional<Doctor> findDoctor(String id) {
        return doctors.stream().filter(d -> d.getId().equals(id)).findFirst();
    }

    public String doctorName(String id) {
        return findDoctor(id).map(Doctor::getName).orElse("(deleted)");
    }

    public String doctorSpecialization(String id) {
        return findDoctor(id).map(Doctor::getSpecialization).orElse("-");
    }

    private Doctor findDoctorOrThrow(String id) throws RecordNotFoundException {
        return findDoctor(id).orElseThrow(() -> new RecordNotFoundException("No doctor found with ID " + id));
    }

    private void validateDoctor(String name, String specialization, String contact,
                                String availability) throws ValidationException {
        validateName(name);
        if (specialization == null || specialization.isBlank()) {
            throw new ValidationException("Please select a specialization.");
        }
        validateContact(contact);
        if (availability == null || availability.isBlank()) {
            throw new ValidationException("Please select the doctor's availability.");
        }
    }

    // ============================ APPOINTMENTS ============================

    public List<Appointment> getAppointments() {
        List<Appointment> copy = new ArrayList<>(appointments);
        copy.sort(Comparator.comparing(Appointment::getDate)
                .thenComparing(Appointment::getTime)
                .thenComparing(Appointment::getId));
        return copy;
    }

    public String nextAppointmentId() {
        return nextId("A", appointments.stream().map(Appointment::getId).toList());
    }

    public Appointment bookAppointment(String patientId, String doctorId, LocalDate date,
                                       LocalTime time, AppointmentStatus status) throws HospitalException {
        if (patientId == null) throw new ValidationException("Please select a patient.");
        if (doctorId == null) throw new ValidationException("Please select a doctor.");
        if (date == null) throw new ValidationException("Please choose an appointment date.");
        if (time == null) throw new ValidationException("Please choose an appointment time.");
        if (status == null || status == AppointmentStatus.CANCELLED) {
            throw new ValidationException("Please choose Confirmed or Pending as the status.");
        }
        findPatientOrThrow(patientId);
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new ValidationException("The appointment date and time cannot be in the past.");
        }

        // Conflict check: same doctor, same date, same time, and not cancelled.
        for (Appointment existing : appointments) {
            if (existing.isActive()
                    && existing.getDoctorId().equals(doctorId)
                    && existing.getDate().equals(date)
                    && existing.getTime().equals(time)) {
                throw new SlotConflictException(doctor.getName() + " is already booked on "
                        + date.format(AppConstants.DATE_FORMAT) + " at "
                        + time.format(AppConstants.TIME_FORMAT)
                        + ". Please choose a different time slot.");
            }
        }

        Appointment a = new Appointment(nextAppointmentId(), patientId, doctorId, date, time, status);
        appointments.add(a);
        fileManager.saveAppointments(appointments);
        return a;
    }

    public void cancelAppointment(String id) throws HospitalException {
        Appointment a = appointments.stream().filter(x -> x.getId().equals(id)).findFirst()
                .orElseThrow(() -> new RecordNotFoundException("No appointment found with ID " + id));
        if (!a.isActive()) {
            throw new ValidationException("Appointment " + id + " is already cancelled.");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        fileManager.saveAppointments(appointments);
    }

    public List<Appointment> getAppointmentsOn(LocalDate date) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getDate().equals(date)) result.add(a);
        }
        result.sort(Comparator.comparing(Appointment::getTime).thenComparing(Appointment::getId));
        return result;
    }

    public List<Appointment> searchAppointments(String query) {
        String q = normalize(query);
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : getAppointments()) {
            if (q.isEmpty() || matches(q, a.getId(), a.getPatientId(), a.getDoctorId(),
                    patientName(a.getPatientId()), doctorName(a.getDoctorId()))) {
                result.add(a);
            }
        }
        return result;
    }

    // ============================ STATISTICS ============================

    public int getPatientCount() { return patients.size(); }
    public int getDoctorCount() { return doctors.size(); }

    /** Today's appointments that are not cancelled. */
    public int getTodayAppointmentCount() {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Appointment a : appointments) {
            if (a.getDate().equals(today) && a.isActive()) count++;
        }
        return count;
    }

    // ============================ SHARED HELPERS ============================

    private void validateName(String name) throws ValidationException {
        if (name == null || name.isBlank()) throw new ValidationException("Name is required.");
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            throw new ValidationException("Name may contain only letters, spaces, dots, apostrophes and hyphens (2 to 60 characters).");
        }
    }

    private void validateContact(String contact) throws ValidationException {
        if (contact == null || contact.isBlank()) throw new ValidationException("Contact number is required.");
        if (!CONTACT_PATTERN.matcher(contact.trim()).matches()) {
            throw new ValidationException("Contact number must be exactly 10 digits.");
        }
    }

    private static String nextId(String prefix, List<String> existingIds) {
        int max = 0;
        for (String id : existingIds) {
            try {
                max = Math.max(max, Integer.parseInt(id.substring(prefix.length())));
            } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                // an ID in an unexpected format is simply ignored
            }
        }
        return String.format("%s%03d", prefix, max + 1);
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static boolean matches(String q, String... fields) {
        for (String f : fields) {
            if (f != null && f.toLowerCase().contains(q)) return true;
        }
        return false;
    }
}
