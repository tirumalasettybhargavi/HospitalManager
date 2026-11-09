package com.hospital.hospitalmanager.view;

import com.hospital.hospitalmanager.model.AppointmentStatus;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Function;

/** Small helpers that build the same kinds of UI pieces on every page. */
public final class UiKit {
    private UiKit() { }

    public static Button button(String text, String styleClass) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        return b;
    }

    public static VBox pageHeader(String title, String subtitle) {
        Label t = new Label(title);
        t.getStyleClass().add("page-title");
        Label s = new Label(subtitle);
        s.getStyleClass().add("page-subtitle");
        return new VBox(2, t, s);
    }

    /** A labelled input: small caption above the control. */
    public static VBox field(String caption, Node control) {
        Label label = new Label(caption);
        label.getStyleClass().add("field-label");
        VBox box = new VBox(5, label, control);
        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        return box;
    }

    /** The dark teal bar at the top of a card. */
    public static HBox cardHeader(String title, Node icon, Node... trailing) {
        Label label = new Label(title);
        label.getStyleClass().add("card-header-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("card-header");
        if (icon != null) header.getChildren().add(icon);
        header.getChildren().addAll(label, spacer);
        header.getChildren().addAll(trailing);
        return header;
    }

    public static VBox card() {
        VBox card = new VBox();
        card.getStyleClass().add("card");
        return card;
    }

    public static ScrollPane scroll(Node content) {
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("page-scroll");
        return sp;
    }

    public static <T> TableView<T> table() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No records found"));
        return table;
    }

    public static <T> TableColumn<T, String> column(String title, Function<T, String> getter, double width) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        col.setPrefWidth(width);
        return col;
    }

    /** A column that shows the appointment status as a coloured badge. */
    public static <T> TableColumn<T, String> statusColumn(String title, Function<T, AppointmentStatus> getter, double width) {
        TableColumn<T, String> col = column(title, item -> getter.apply(item).getLabel(), width);
        col.setCellFactory(c -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);
                setText(null);
                if (empty || text == null) {
                    setGraphic(null);
                } else {
                    setGraphic(badge(AppointmentStatus.fromLabel(text)));
                }
            }
        });
        return col;
    }

    public static Label badge(AppointmentStatus status) {
        Label label = new Label(status.getLabel());
        label.getStyleClass().add("badge");
        switch (status) {
            case CONFIRMED -> label.getStyleClass().add("badge-confirmed");
            case PENDING -> label.getStyleClass().add("badge-pending");
            case CANCELLED -> label.getStyleClass().add("badge-cancelled");
        }
        return label;
    }

    public static Insets pagePadding() {
        return new Insets(10, 24, 24, 24);
    }
}
