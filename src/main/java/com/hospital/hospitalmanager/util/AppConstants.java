package com.hospital.hospitalmanager.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Fixed lists and date/time formats shared by the whole application. */
public final class AppConstants {
    private AppConstants() { }

    public static final List<String> SPECIALIZATIONS = List.of(
            "Cardiology", "Dermatology", "General Medicine", "Pediatrics", "Orthopedics",
            "Gynecology", "Neurology", "Ophthalmology", "ENT", "Dentistry");

    public static final List<String> GENDERS = List.of("Male", "Female", "Other");

    public static final List<String> AVAILABILITY = List.of(
            "Mon - Fri (9 AM - 5 PM)", "Mon - Sat (9 AM - 1 PM)",
            "Mon, Wed, Fri", "Tue, Thu, Sat", "Weekends only");

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
    public static final DateTimeFormatter HEADER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.ENGLISH);

    /** Appointment slots: every 30 minutes from 9:00 AM to 4:30 PM. */
    public static List<LocalTime> timeSlots() {
        List<LocalTime> slots = new ArrayList<>();
        for (int hour = 9; hour < 17; hour++) {
            slots.add(LocalTime.of(hour, 0));
            slots.add(LocalTime.of(hour, 30));
        }
        return slots;
    }
}
