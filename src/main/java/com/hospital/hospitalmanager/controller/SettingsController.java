package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.StorageException;
import com.hospital.hospitalmanager.view.Dialogs;
import com.hospital.hospitalmanager.view.Icons;
import com.hospital.hospitalmanager.view.UiKit;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/** Settings page: shows where data is stored and lets the user reload it from the files. */
public class SettingsController implements PageController {
    private final HospitalManager manager;
    private final ScrollPane view;
    private final Label folderLabel = new Label();
    private final Label countsLabel = new Label();

    public SettingsController(HospitalManager manager) {
        this.manager = manager;

        folderLabel.getStyleClass().add("about-text");
        folderLabel.setWrapText(true);
        countsLabel.getStyleClass().add("about-text");

        Label note = new Label("Patients, doctors and appointments are saved automatically after every change. "
                + "Use the button below if you edited the files by hand and want to load them again.");
        note.getStyleClass().add("about-italic");
        note.setWrapText(true);

        Button reload = UiKit.button("Reload Data from Files", "primary-button");
        reload.setOnAction(e -> reload());

        VBox body = new VBox(12, new Label("Data folder:"), folderLabel, countsLabel, note, reload);
        body.setPadding(new Insets(18));

        VBox card = UiKit.card();
        card.getChildren().addAll(UiKit.cardHeader("Data and Storage", Icons.of(Icons.GEAR, 24, Color.WHITE)), body);

        VBox page = new VBox(16,
                UiKit.pageHeader("Settings", "Storage information for SmartCare Hospital."),
                card);
        page.setPadding(UiKit.pagePadding());
        view = UiKit.scroll(page);
    }

    @Override
    public Parent getView() { return view; }

    @Override
    public void refresh() {
        folderLabel.setText(manager.getFileManager().getDataDirectory().toString());
        countsLabel.setText(manager.getPatientCount() + " patients, "
                + manager.getDoctorCount() + " doctors, "
                + manager.getAppointments().size() + " appointments stored.");
    }

    private void reload() {
        try {
            manager.load();
            refresh();
            List<String> warnings = manager.getLoadWarnings();
            if (warnings.isEmpty()) {
                Dialogs.info("Data was reloaded from the files.");
            } else {
                Dialogs.warning("Data was reloaded, but some lines were skipped:\n\n" + String.join("\n", warnings));
            }
        } catch (StorageException ex) {
            Dialogs.error(ex.getMessage());
        }
    }
}
