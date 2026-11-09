package com.hospital.hospitalmanager.application;

import com.hospital.hospitalmanager.controller.MainController;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.FileManager;
import com.hospital.hospitalmanager.util.StorageException;
import com.hospital.hospitalmanager.view.Dialogs;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        HospitalManager manager = new HospitalManager(new FileManager());
        String loadError = null;
        try {
            manager.load();
        } catch (StorageException e) {
            loadError = e.getMessage();
        }

        MainController main = new MainController(manager);
        Scene scene = new Scene(main.getRoot(), 1320, 800);
        scene.getStylesheets().add(Dialogs.stylesheet());

        stage.setTitle("SmartCare Hospital - Patient and Appointment Manager");
        stage.setMinWidth(1150);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();

        // Show any problems found while loading the files.
        if (loadError != null) {
            Dialogs.error("Saved data could not be loaded: " + loadError);
        }
        List<String> warnings = manager.getLoadWarnings();
        if (!warnings.isEmpty()) {
            Dialogs.warning("Some saved records were skipped:\n\n" + String.join("\n", warnings));
        }
    }
}
