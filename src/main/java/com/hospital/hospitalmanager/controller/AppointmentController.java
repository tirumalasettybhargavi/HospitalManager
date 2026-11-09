package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.model.AppointmentStatus;
import com.hospital.hospitalmanager.model.Doctor;
import com.hospital.hospitalmanager.model.Patient;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.util.HospitalException;
import com.hospital.hospitalmanager.view.Dialogs;
import com.hospital.hospitalmanager.view.UiKit;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;

/** Appointment Management page: book, cancel, search and list appointments. */
public class AppointmentController implements PageController {
    private final HospitalManager manager;
    private final ScrollPane view;

    private final TextField idField = new TextField();
    private final ComboBox<Patient> patientBox = new ComboBox<>();
    private final ComboBox<Doctor> doctorBox = new ComboBox<>();
    private final TextField specializationField = new TextField();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final ComboBox<LocalTime> timeBox = new ComboBox<>();
    private final ComboBox<AppointmentStatus> statusBox = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final TableView<Appointment> table = UiKit.table();

    private final Button bookButton = UiKit.button("Book Appointment", "primary-button");
    private final Button cancelButton = UiKit.button("Cancel Appointment", "danger-button");
    private final Button clearButton = UiKit.button("Clear", "secondary-button");

    public AppointmentController(HospitalManager manager) {
        this.manager = manager;

        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");
        specializationField.setEditable(false);
        specializationField.getStyleClass().add("readonly-field");
        specializationField.setPromptText("Filled in when a doctor is chosen");

        patientBox.setPromptText("Select patient");
        patientBox.setMaxWidth(Double.MAX_VALUE);
        doctorBox.setPromptText("Select doctor");
        doctorBox.setMaxWidth(Double.MAX_VALUE);
        doctorBox.valueProperty().addListener((obs, oldValue, d) ->
                specializationField.setText(d == null ? "" : d.getSpecialization()));

        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setEditable(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        timeBox.getItems().addAll(AppConstants.timeSlots());
        timeBox.setPromptText("Select time");
        timeBox.setMaxWidth(Double.MAX_VALUE);
        timeBox.setConverter(new StringConverter<LocalTime>() {
            @Override
            public String toString(LocalTime time) {
                return time == null ? "" : time.format(AppConstants.TIME_FORMAT);
            }

            @Override
            public LocalTime fromString(String text) {
                return null;
            }
        });

        statusBox.getItems().addAll(AppointmentStatus.CONFIRMED, AppointmentStatus.PENDING);
        statusBox.setValue(AppointmentStatus.CONFIRMED);
        statusBox.setMaxWidth(Double.MAX_VALUE);

        searchField.setPromptText("Search by Appointment ID, patient name or doctor name");

        VBox page = new VBox(16,
                UiKit.pageHeader("Appointment Management", "Book appointments. A doctor cannot be booked twice for the same date and time."),
                buildFormCard(),
                buildTableCard());
        page.setPadding(UiKit.pagePadding());
        view = UiKit.scroll(page);

        wireActions();
    }

    @Override
    public Parent getView() { return view; }

    @Override
    public void refresh() {
        Patient selectedPatient = patientBox.getValue();
        Doctor selectedDoctor = doctorBox.getValue();
        patientBox.setItems(FXCollections.observableArrayList(manager.getPatients()));
        doctorBox.setItems(FXCollections.observableArrayList(manager.getDoctors()));
        patientBox.setValue(selectedPatient == null ? null
                : manager.findPatient(selectedPatient.getId()).orElse(null));
        doctorBox.setValue(selectedDoctor == null ? null
                : manager.findDoctor(selectedDoctor.getId()).orElse(null));
        loadTable();
        if (table.getSelectionModel().getSelectedItem() == null) {
            idField.setText(manager.nextAppointmentId());
        }
    }

    // ============================ LAYOUT ============================

    private VBox buildFormCard() {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        ColumnConstraints half = new ColumnConstraints();
        half.setPercentWidth(50);
        grid.getColumnConstraints().addAll(half, half);

        grid.add(UiKit.field("Appointment ID (automatic)", idField), 0, 0);
        grid.add(UiKit.field("Status", statusBox), 1, 0);
        grid.add(UiKit.field("Patient ID / Patient Name *", patientBox), 0, 1);
        grid.add(UiKit.field("Doctor ID / Doctor Name *", doctorBox), 1, 1);
        grid.add(UiKit.field("Specialization", specializationField), 0, 2);
        grid.add(UiKit.field("Appointment Date *", datePicker), 1, 2);
        grid.add(UiKit.field("Appointment Time *", timeBox), 0, 3);

        HBox buttons = new HBox(12, bookButton, cancelButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(16, grid, buttons);
        body.setPadding(new Insets(18));

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("Appointment Details", null), body);
        return card;
    }

    private VBox buildTableCard() {
        Button searchButton = UiKit.button("Search Appointment", "primary-button");
        Button showAll = UiKit.button("Show All", "secondary-button");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchButton.setOnAction(e -> loadTable());
        showAll.setOnAction(e -> {
            searchField.clear();
            loadTable();
        });
        searchField.setOnAction(e -> loadTable());

        HBox searchRow = new HBox(12, searchField, searchButton, showAll);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.setPadding(new Insets(14, 18, 10, 18));

        table.getColumns().add(UiKit.<Appointment>column("ID", Appointment::getId, 70));
        table.getColumns().add(UiKit.<Appointment>column("Patient",
                a -> a.getPatientId() + " - " + manager.patientName(a.getPatientId()), 170));
        table.getColumns().add(UiKit.<Appointment>column("Doctor",
                a -> a.getDoctorId() + " - " + manager.doctorName(a.getDoctorId()), 170));
        table.getColumns().add(UiKit.<Appointment>column("Specialization",
                a -> manager.doctorSpecialization(a.getDoctorId()), 130));
        table.getColumns().add(UiKit.<Appointment>column("Date",
                a -> a.getDate().format(AppConstants.DATE_FORMAT), 110));
        table.getColumns().add(UiKit.<Appointment>column("Time",
                a -> a.getTime().format(AppConstants.TIME_FORMAT), 90));
        table.getColumns().add(UiKit.<Appointment>statusColumn("Status", Appointment::getStatus, 110));
        table.setPrefHeight(320);
        table.setMinHeight(320);

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("All Appointments", null), searchRow, table);
        return card;
    }

    // ============================ ACTIONS ============================

    private void wireActions() {
        bookButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        cancelButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // Selecting a row shows its details in the form.
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, a) -> {
            if (a != null) {
                idField.setText(a.getId());
                patientBox.setValue(manager.findPatient(a.getPatientId()).orElse(null));
                doctorBox.setValue(manager.findDoctor(a.getDoctorId()).orElse(null));
                datePicker.setValue(a.getDate());
                timeBox.setValue(a.getTime());
                if (a.isActive()) {
                    statusBox.setValue(a.getStatus());
                }
            }
        });

        bookButton.setOnAction(e -> book());
        cancelButton.setOnAction(e -> cancel());
        clearButton.setOnAction(e -> clearForm());
    }

    private void book() {
        try {
            Patient patient = patientBox.getValue();
            Doctor doctor = doctorBox.getValue();
            Appointment a = manager.bookAppointment(
                    patient == null ? null : patient.getId(),
                    doctor == null ? null : doctor.getId(),
                    datePicker.getValue(), timeBox.getValue(), statusBox.getValue());
            Dialogs.info("Appointment " + a.getId() + " booked for "
                    + manager.patientName(a.getPatientId()) + " with "
                    + manager.doctorName(a.getDoctorId()) + " on "
                    + a.getDate().format(AppConstants.DATE_FORMAT) + " at "
                    + a.getTime().format(AppConstants.TIME_FORMAT) + ".");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void cancel() {
        Appointment selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Click an appointment in the table first.");
            return;
        }
        if (!Dialogs.confirm("Cancel appointment " + selected.getId() + "?")) {
            return;
        }
        try {
            manager.cancelAppointment(selected.getId());
            Dialogs.info("Appointment " + selected.getId() + " was cancelled. The time slot is free again.");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        patientBox.setValue(null);
        doctorBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        timeBox.setValue(null);
        statusBox.setValue(AppointmentStatus.CONFIRMED);
        loadTable();
        idField.setText(manager.nextAppointmentId());
    }

    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(manager.searchAppointments(searchField.getText())));
    }
}
