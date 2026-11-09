package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Doctor;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Doctor Management page: add, update, delete, search and list doctors. */
public class DoctorController implements PageController {
    private final HospitalManager manager;
    private final ScrollPane view;

    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final ComboBox<String> specializationBox = new ComboBox<>();
    private final TextField contactField = new TextField();
    private final ComboBox<String> availabilityBox = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final TableView<Doctor> table = UiKit.table();

    private final Button addButton = UiKit.button("Add Doctor", "primary-button");
    private final Button updateButton = UiKit.button("Update Doctor", "secondary-button");
    private final Button deleteButton = UiKit.button("Delete Doctor", "danger-button");
    private final Button clearButton = UiKit.button("Clear", "secondary-button");

    public DoctorController(HospitalManager manager) {
        this.manager = manager;

        idField.setEditable(false);
        idField.getStyleClass().add("readonly-field");
        nameField.setPromptText("Doctor's full name");
        specializationBox.getItems().addAll(AppConstants.SPECIALIZATIONS);
        specializationBox.setPromptText("Select specialization");
        specializationBox.setMaxWidth(Double.MAX_VALUE);
        contactField.setPromptText("10-digit mobile number");
        availabilityBox.getItems().addAll(AppConstants.AVAILABILITY);
        availabilityBox.setPromptText("Select availability");
        availabilityBox.setMaxWidth(Double.MAX_VALUE);
        searchField.setPromptText("Search by Doctor ID, name or specialization");

        VBox page = new VBox(16,
                UiKit.pageHeader("Doctor Management", "Maintain doctor details, specializations and availability."),
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
            idField.setText(manager.nextDoctorId());
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

        grid.add(UiKit.field("Doctor ID (automatic)", idField), 0, 0);
        grid.add(UiKit.field("Doctor Name *", nameField), 1, 0);
        grid.add(UiKit.field("Specialization *", specializationBox), 0, 1);
        grid.add(UiKit.field("Contact Number *", contactField), 1, 1);
        grid.add(UiKit.field("Availability *", availabilityBox), 0, 2);

        HBox buttons = new HBox(12, addButton, updateButton, deleteButton, clearButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(16, grid, buttons);
        body.setPadding(new Insets(18));

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("Doctor Details", null), body);
        return card;
    }

    private VBox buildTableCard() {
        Button searchButton = UiKit.button("Search Doctor", "primary-button");
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

        table.getColumns().add(UiKit.<Doctor>column("ID", Doctor::getId, 70));
        table.getColumns().add(UiKit.<Doctor>column("Name", Doctor::getName, 160));
        table.getColumns().add(UiKit.<Doctor>column("Specialization", Doctor::getSpecialization, 140));
        table.getColumns().add(UiKit.<Doctor>column("Contact", Doctor::getContact, 110));
        table.getColumns().add(UiKit.<Doctor>column("Availability", Doctor::getAvailability, 180));
        table.setPrefHeight(300);
        table.setMinHeight(300);

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("All Doctors", null), searchRow, table);
        return card;
    }

    // ============================ ACTIONS ============================

    private void wireActions() {
        addButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNotNull());
        updateButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, d) -> {
            if (d != null) {
                idField.setText(d.getId());
                nameField.setText(d.getName());
                specializationBox.setValue(d.getSpecialization());
                contactField.setText(d.getContact());
                availabilityBox.setValue(d.getAvailability());
            }
        });

        addButton.setOnAction(e -> addDoctor());
        updateButton.setOnAction(e -> updateDoctor());
        deleteButton.setOnAction(e -> deleteDoctor());
        clearButton.setOnAction(e -> clearForm());
    }

    private void addDoctor() {
        try {
            Doctor d = manager.addDoctor(nameField.getText(), specializationBox.getValue(),
                    contactField.getText(), availabilityBox.getValue());
            Dialogs.info(d.getName() + " was added with ID " + d.getId() + ".");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void updateDoctor() {
        Doctor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Click a doctor in the table first.");
            return;
        }
        try {
            manager.updateDoctor(selected.getId(), nameField.getText(), specializationBox.getValue(),
                    contactField.getText(), availabilityBox.getValue());
            Dialogs.info("Doctor " + selected.getId() + " was updated.");
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void deleteDoctor() {
        Doctor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.error("Click a doctor in the table first.");
            return;
        }
        if (!Dialogs.confirm("Delete " + selected.getName() + " (" + selected.getId() + ")?")) {
            return;
        }
        try {
            manager.deleteDoctor(selected.getId());
            clearForm();
        } catch (HospitalException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void clearForm() {
        table.getSelectionModel().clearSelection();
        nameField.clear();
        specializationBox.setValue(null);
        contactField.clear();
        availabilityBox.setValue(null);
        loadTable();
        idField.setText(manager.nextDoctorId());
    }

    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(manager.searchDoctors(searchField.getText())));
    }
}
