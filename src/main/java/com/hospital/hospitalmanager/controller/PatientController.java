package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Patient;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.util.HospitalException;
import com.hospital.hospitalmanager.util.ValidationException;
import com.hospital.hospitalmanager.view.Dialogs;
import com.hospital.hospitalmanager.view.UiKit;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Patient Management page: register, update, delete, search and list patients. */
public class PatientController implements PageController {
    private final HospitalManager manager;
    private final ScrollPane view;

    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField ageField = new TextField();
    private final ComboBox<String> genderBox = new ComboBox<>();
    private final TextField contactField = new TextField();
    private final TextArea addressArea = new TextArea();
    private final TextArea historyArea = new TextArea();
    private final TextField searchField = new TextField();
    private final TableView<Patient> table = UiKit.table();

    private final Button saveButton = UiKit.button("Save Patient", "primary-button");
    private final Button updateButton = UiKit.button("Update Patient", "secondary-button");
    private final Button deleteButton = UiKit.button("Delete Patient", "danger-button");
    private final Button clearButton = UiKit.button("Clear", "secondary-button");

    public PatientController(HospitalManager manager) {
        this.manager = manager;

        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");
        nameField.setPromptText("Full name");
        ageField.setPromptText("Age in years");
        genderBox.getItems().addAll(AppConstants.GENDERS);
        genderBox.setPromptText("Select gender");
        genderBox.setMaxWidth(Double.MAX_VALUE);
        contactField.setPromptText("10-digit mobile number");
        addressArea.setPromptText("Address");
        addressArea.setPrefRowCount(2);
        addressArea.setWrapText(true);
        historyArea.setPromptText("Allergies, past illnesses, ongoing treatment...");
        historyArea.setPrefRowCount(3);
        historyArea.setWrapText(true);
        searchField.setPromptText("Search by Patient ID, name or contact number");

        VBox page = new VBox(16,
                UiKit.pageHeader("Patient Management", "Register patients and keep their contact and medical details up to date."),
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
        loadTable();
        if (table.getSelectionModel().getSelectedItem() == null) {
            idField.setText(manager.nextPatientId());
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

        grid.add(UiKit.field("Patient ID (automatic)", idField), 0, 0);
        grid.add(UiKit.field("Patient Name *", nameField), 1, 0);
        grid.add(UiKit.field("Age *", ageField), 0, 1);
        grid.add(UiKit.field("Gender *", genderBox), 1, 1);
        grid.add(UiKit.field("Contact Number *", contactField), 0, 2);
        grid.add(UiKit.field("Address", addressArea), 1, 2);
        grid.add(UiKit.field("Basic Medical History", historyArea), 0, 3, 2, 1);

        HBox buttons = new HBox(12, saveButton, updateButton, deleteButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(16, grid, buttons);
        body.setPadding(new Insets(18));

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("Patient Details", null), body);
        return card;
    }

    private VBox buildTableCard() {
        Button searchButton = UiKit.button("Search Patient", "primary-button");
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

        table.getColumns().add(UiKit.<Patient>column("ID", Patient::getId, 70));
        table.getColumns().add(UiKit.<Patient>column("Name", Patient::getName, 150));
        table.getColumns().add(UiKit.<Patient>column("Age", p -> String.valueOf(p.getAge()), 50));
        table.getColumns().add(UiKit.<Patient>column("Gender", Patient::getGender, 80));
        table.getColumns().add(UiKit.<Patient>column("Contact", Patient::getContact, 110));
        table.getColumns().add(UiKit.<Patient>column("Address", Patient::getAddress, 160));
        table.getColumns().add(UiKit.<Patient>column("Medical History", Patient::getMedicalHistory, 200));
        table.setPrefHeight(300);
        table.setMinHeight(300);

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("All Patients", null), searchRow, table);
        return card;
    }

    // ============================ ACTIONS ============================

    private void wireActions() {
        // Save is only for new patients; Update and Delete need a selected row.
        saveButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        updateButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, p) -> {
            if (p != null) {
                idField.setText(p.getId());
                nameField.setText(p.getName());
                ageField.setText(String.valueOf(p.getAge()));
                genderBox.setValue(p.getGender());
                contactField.setText(p.getContact());
                addressArea.setText(p.getAddress());
                historyArea.setText(p.getMedicalHistory());
            }
        });

        saveButton.setOnAction(e -> savePatient());
        updateButton.setOnAction(e -> updatePatient());
        deleteButton.setOnAction(e -> deletePatient());
        clearButton.setOnAction(e -> clearForm());
    }

    private void savePatient() {
        try {
            Patient p = manager.addPatient(nameField.getText(), parseAge(), genderBox.getValue(),
                    contactField.getText(), addressArea.getText(), historyArea.getText());
            Dialogs.info("Patient " + p.getName() + " was registered with ID " + p.getId() + ".");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void updatePatient() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Click a patient in the table first.");
            return;
        }
        try {
            manager.updatePatient(selected.getId(), nameField.getText(), parseAge(), genderBox.getValue(),
                    contactField.getText(), addressArea.getText(), historyArea.getText());
            Dialogs.info("Patient " + selected.getId() + " was updated.");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void deletePatient() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Click a patient in the table first.");
            return;
        }
        if (!Dialogs.confirm("Delete patient " + selected.getName() + " (" + selected.getId() + ")?")) {
            return;
        }
        try {
            manager.deletePatient(selected.getId());
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private int parseAge() throws ValidationException {
        String text = ageField.getText() == null ? "" : ageField.getText().trim();
        if (text.isEmpty()) {
            throw new ValidationException("Age is required.");
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Age must be a whole number.");
        }
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        nameField.clear();
        ageField.clear();
        genderBox.setValue(null);
        contactField.clear();
        addressArea.clear();
        historyArea.clear();
        loadTable();
        idField.setText(manager.nextPatientId());
    }

    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(manager.searchPatients(searchField.getText())));
    }
}
