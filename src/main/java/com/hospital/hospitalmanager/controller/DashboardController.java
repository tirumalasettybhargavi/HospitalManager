package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.model.Appointment;
import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.view.Icons;
import com.hospital.hospitalmanager.view.Logo;
import com.hospital.hospitalmanager.view.UiKit;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;

/** The home page: statistics, today's appointments, quick actions and welcome panels. */
public class DashboardController implements PageController {
    private static final Color TEAL = Color.web("#008F91");
    private static final Color DARK = Color.web("#075E60");

    private final HospitalManager manager;
    private final Navigator navigator;
    private final ScrollPane view;
    private final Label patientsValue = new Label("0");
    private final Label doctorsValue = new Label("0");
    private final Label todayValue = new Label("0");
    private final TableView<Appointment> table = UiKit.table();

    public DashboardController(HospitalManager manager, Navigator navigator) {
        this.manager = manager;
        this.navigator = navigator;

        VBox page = new VBox(18, buildStats(), buildMiddle(), buildBottom());
        page.setPadding(UiKit.pagePadding());
        view = UiKit.scroll(page);
    }

    @Override
    public Parent getView() { return view; }

    @Override
    public void refresh() {
        patientsValue.setText(String.valueOf(manager.getPatientCount()));
        doctorsValue.setText(String.valueOf(manager.getDoctorCount()));
        todayValue.setText(String.valueOf(manager.getTodayAppointmentCount()));
        table.setItems(FXCollections.observableArrayList(manager.getAppointmentsOn(LocalDate.now())));
    }

    // ============================ STATISTICS ============================

    private HBox buildStats() {
        HBox row = new HBox(18,
                statCard(Icons.of(Icons.PEOPLE, 70, TEAL), patientsValue, "Total Patients", false),
                statCard(Icons.of(Icons.DOCTOR, 64, DARK), doctorsValue, "Total Doctors", false),
                statCard(Icons.of(Icons.CALENDAR, 64, DARK), todayValue, "Today's Appointments", true));
        return row;
    }

    private HBox statCard(Node icon, Label value, String caption, boolean green) {
        value.getStyleClass().add("stat-value");
        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("stat-label");
        HBox card = new HBox(24, icon, new VBox(0, value, captionLabel));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 28, 18, 28));
        card.getStyleClass().add("stat-card");
        if (green) {
            card.getStyleClass().add("stat-card-green");
        }
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPrefWidth(10);
        return card;
    }

    // ============================ TODAY'S APPOINTMENTS + QUICK ACTIONS ============================

    private HBox buildMiddle() {
        VBox appointments = buildAppointmentsCard();
        VBox quick = buildQuickActions();
        HBox.setHgrow(appointments, Priority.ALWAYS);
        HBox row = new HBox(18, appointments, quick);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private VBox buildAppointmentsCard() {
        Button viewAll = UiKit.button("View All", "outline-button");
        viewAll.setGraphic(Icons.of(Icons.ARROW, 16, Color.WHITE));
        viewAll.setContentDisplay(ContentDisplay.RIGHT);
        viewAll.setOnAction(e -> navigator.navigate(Page.SCHEDULE));

        table.getColumns().add(UiKit.<Appointment>column("Time",
                a -> a.getTime().format(AppConstants.TIME_FORMAT), 90));
        table.getColumns().add(UiKit.<Appointment>column("Patient Name",
                a -> manager.patientName(a.getPatientId()), 150));
        table.getColumns().add(UiKit.<Appointment>column("Doctor Name",
                a -> manager.doctorName(a.getDoctorId()), 150));
        table.getColumns().add(UiKit.<Appointment>column("Specialization",
                a -> manager.doctorSpecialization(a.getDoctorId()), 140));
        table.getColumns().add(UiKit.<Appointment>statusColumn("Status", Appointment::getStatus, 110));
        table.setPrefHeight(290);
        table.setMinHeight(290);

        VBox card = UiKit.card();
        card.getChildren().addAll(
                UiKit.cardHeader("Today's Appointments", Icons.of(Icons.CALENDAR, 24, Color.WHITE), viewAll),
                table);
        return card;
    }

    private VBox buildQuickActions() {
        VBox body = new VBox(12,
                quickAction("Register Patient", Icons.PERSON_ADD, () -> navigator.navigate(Page.PATIENTS)),
                quickAction("Add Doctor", Icons.DOCTOR, () -> navigator.navigate(Page.DOCTORS)),
                quickAction("Book Appointment", Icons.CALENDAR, () -> navigator.navigate(Page.APPOINTMENTS)),
                quickAction("Search Records", Icons.SEARCH, navigator::openSearch));
        body.setPadding(new Insets(14));

        VBox card = UiKit.card();
        card.setPrefWidth(300);
        card.setMinWidth(300);
        card.getChildren().addAll(
                UiKit.cardHeader("Quick Actions", Icons.of(Icons.BOLT, 24, Color.WHITE)),
                body);
        return card;
    }

    private Button quickAction(String text, String iconPath, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("quick-action");
        button.setGraphic(Icons.of(iconPath, 34, DARK));
        button.setGraphicTextGap(18);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    // ============================ WELCOME + ABOUT ============================

    private HBox buildBottom() {
        VBox welcome = buildWelcomeCard();
        VBox about = buildAboutCard();
        HBox.setHgrow(welcome, Priority.ALWAYS);
        HBox.setHgrow(about, Priority.ALWAYS);
        welcome.setPrefWidth(620);
        about.setPrefWidth(440);
        HBox row = new HBox(18, welcome, about);
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private VBox buildWelcomeCard() {
        Label title = new Label("Welcome to SmartCare Hospital!");
        title.getStyleClass().add("welcome-title");
        Label text = new Label("Efficiently manage patients, doctors and appointments with ease and convenience.");
        text.getStyleClass().add("welcome-text");
        text.setWrapText(true);
        Label tagline = new Label("Better Management. Better Care.");
        tagline.getStyleClass().add("tagline-pill");
        tagline.setMaxWidth(Double.MAX_VALUE);
        tagline.setAlignment(Pos.CENTER);

        VBox right = new VBox(10, title, text, tagline);
        right.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox inner = new HBox(22, Logo.building(), right);
        inner.setAlignment(Pos.CENTER_LEFT);
        inner.setPadding(new Insets(16, 22, 16, 22));

        VBox card = UiKit.card();
        card.getStyleClass().add("welcome-card");
        card.getChildren().add(inner);
        return card;
    }

    private VBox buildAboutCard() {
        Label line1 = new Label("SmartCare Hospital Patient and Appointment Manager");
        line1.getStyleClass().add("about-text");
        line1.setWrapText(true);
        Label line2 = new Label("Built with JavaFX");
        line2.getStyleClass().add("about-italic");
        Label motto = new Label("Healthy Community. Happy Lives.");
        motto.getStyleClass().add("about-motto");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox mottoRow = new HBox(10, motto, spacer, Logo.heart(54, TEAL, Color.WHITE));
        mottoRow.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(8, line1, line2, new Separator(), mottoRow);
        body.setPadding(new Insets(14, 18, 14, 18));

        VBox card = UiKit.card();
        card.getChildren().addAll(
                UiKit.cardHeader("About This System", Icons.of(Icons.INFO, 24, Color.WHITE)),
                body);
        return card;
    }
}
