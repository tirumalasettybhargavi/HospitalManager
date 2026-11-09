package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.view.UiKit;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/** Daily Schedule page: pick a date and see every appointment on that day. */
public class ScheduleController implements PageController {
    private final HospitalManager manager;
    private final ScrollPane view;
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final Label summary = new Label();
    private final TableView<Appointment> table = UiKit.table();

    public ScheduleController(HospitalManager manager) {
        this.manager = manager;

        datePicker.setEditable(false);
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> loadTable());

        Button today = UiKit.button("Today", "secondary-button");
        today.setOnAction(e -> datePicker.setValue(LocalDate.now()));
        Button previous = UiKit.button("\u25C0  Previous Day", "secondary-button");
        previous.setOnAction(e -> shiftDay(-1));
        Button next = UiKit.button("Next Day  \u25B6", "secondary-button");
        next.setOnAction(e -> shiftDay(1));

        summary.getStyleClass().add("summary-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controls = new HBox(12, UiKit.field("Select Date", datePicker), previous, today, next, spacer, summary);
        controls.setAlignment(Pos.BOTTOM_LEFT);
        controls.setPadding(new Insets(18));

        table.getColumns().add(UiKit.<Appointment>column("Appointment ID", Appointment::getId, 110));
        table.getColumns().add(UiKit.<Appointment>column("Patient Name",
                a -> manager.patientName(a.getPatientId()), 150));
        table.getColumns().add(UiKit.<Appointment>column("Doctor Name",
                a -> manager.doctorName(a.getDoctorId()), 150));
        table.getColumns().add(UiKit.<Appointment>column("Specialization",
                a -> manager.doctorSpecialization(a.getDoctorId()), 130));
        table.getColumns().add(UiKit.<Appointment>column("Date",
                a -> a.getDate().format(AppConstants.DATE_FORMAT), 110));
        table.getColumns().add(UiKit.<Appointment>column("Time",
                a -> a.getTime().format(AppConstants.TIME_FORMAT), 90));
        table.getColumns().add(UiKit.<Appointment>statusColumn("Status", Appointment::getStatus, 110));
        table.setPrefHeight(440);
        table.setMinHeight(440);

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("Appointments for the Selected Day", null), controls, table);

        VBox page = new VBox(16,
                UiKit.pageHeader("Daily Schedule", "Confirmed, pending and cancelled appointments for any date."),
                card);
        page.setPadding(UiKit.pagePadding());
        view = UiKit.scroll(page);
    }

    @Override
    public Parent getView() { return view; }

    @Override
    public void refresh() {
        loadTable();
    }

    private void shiftDay(int days) {
        LocalDate current = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        datePicker.setValue(current.plusDays(days));
    }

    private void loadTable() {
        LocalDate date = datePicker.getValue();
        if (date == null) {
            table.getItems().clear();
            summary.setText("");
            return;
        }
        List<Appointment> list = manager.getAppointmentsOn(date);
        table.setItems(FXCollections.observableArrayList(list));
        long active = list.stream().filter(Appointment::isActive).count();
        summary.setText(active + " active, " + (list.size() - active) + " cancelled");
    }
}
