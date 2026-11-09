package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.model.Doctor;
import com.hospital.hospitalmanager.model.Patient;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.view.Dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** The Search Records pop-up: one box that searches patients, doctors and appointments. */
public class SearchController {
    private final HospitalManager manager;

    public SearchController(HospitalManager manager) {
        this.manager = manager;
    }

    public void show() {
        TextField field = new TextField();
        field.setPromptText("Patient ID, patient name, Doctor ID, doctor name or Appointment ID");
        HBox.setHgrow(field, Priority.ALWAYS);
        Button go = new Button("Search");
        go.getStyleClass().add("primary-button");

        ListView<String> results = new ListView<>();
        results.setPrefHeight(320);
        results.setPlaceholder(new Label("Type something to search"));

        Runnable search = () -> results.setItems(FXCollections.observableArrayList(find(field.getText())));
        go.setOnAction(e -> search.run());
        field.setOnAction(e -> search.run());
        field.textProperty().addListener((obs, oldText, newText) -> search.run());

        HBox row = new HBox(10, field, go);
        row.setAlignment(Pos.CENTER_LEFT);
        VBox box = new VBox(12, row, results);
        box.setPadding(new Insets(6));
        box.setPrefWidth(760);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Search Records");
        dialog.setHeaderText("Search Records");
        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(Dialogs.stylesheet());
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setContent(box);
        dialog.showAndWait();
    }

    private List<String> find(String query) {
        List<String> lines = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return lines;
        }
        for (Patient p : manager.searchPatients(query)) {
            lines.add("PATIENT   " + p.getId() + "  |  " + p.getName() + "  |  Age " + p.getAge()
                    + "  |  " + p.getContact());
        }
        for (Doctor d : manager.searchDoctors(query)) {
            lines.add("DOCTOR   " + d.getId() + "  |  " + d.getName() + "  |  " + d.getSpecialization()
                    + "  |  " + d.getContact());
        }
        for (Appointment a : manager.searchAppointments(query)) {
            lines.add("APPOINTMENT   " + a.getId() + "  |  " + manager.patientName(a.getPatientId())
                    + " with " + manager.doctorName(a.getDoctorId())
                    + "  |  " + a.getDate().format(AppConstants.DATE_FORMAT)
                    + " " + a.getTime().format(AppConstants.TIME_FORMAT)
                    + "  |  " + a.getStatus().getLabel());
        }
        if (lines.isEmpty()) {
            lines.add("No matching records found.");
        }
        return lines;
    }
}
