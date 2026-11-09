package com.hospital.hospitalmanager.view;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/** Pop-up messages that use the teal stylesheet. */
public final class Dialogs {
    private Dialogs() { }

    public static String stylesheet() {
        return Dialogs.class.getResource("/css/smartcare.css").toExternalForm();
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Success", message);
    }

    public static void warning(String message) {
        show(Alert.AlertType.WARNING, "Warning", message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Please check", message);
    }

    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle("Please confirm");
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add(stylesheet());
        Optional<ButtonType> choice = alert.showAndWait();
        return choice.isPresent() && choice.get() == ButtonType.YES;
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add(stylesheet());
        alert.showAndWait();
    }
}
