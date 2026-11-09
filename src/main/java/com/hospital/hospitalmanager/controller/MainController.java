package com.hospital.hospitalmanager.controller;

import com.hospital.hospitalmanager.service.HospitalManager;
import com.hospital.hospitalmanager.util.AppConstants;
import com.hospital.hospitalmanager.view.Dialogs;
import com.hospital.hospitalmanager.view.Icons;
import com.hospital.hospitalmanager.view.Logo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

/** Builds the window frame (sidebar + header) and switches between pages. */
public class MainController implements Navigator {
    private static final Color DARK = Color.web("#064B4D");

    private final HospitalManager manager;
    private final BorderPane root = new BorderPane();
    private final BorderPane content = new BorderPane();
    private final Map<Page, PageController> pages = new EnumMap<>(Page.class);
    private final Map<Page, Button> navButtons = new EnumMap<>(Page.class);
    private final Map<Page, String> navIcons = new EnumMap<>(Page.class);
    private final Label dateLabel = new Label();
    private final Label timeLabel = new Label();
    private Page current = Page.DASHBOARD;

    public MainController(HospitalManager manager) {
        this.manager = manager;

        pages.put(Page.DASHBOARD, new DashboardController(manager, this));
        pages.put(Page.PATIENTS, new PatientController(manager));
        pages.put(Page.DOCTORS, new DoctorController(manager));
        pages.put(Page.APPOINTMENTS, new AppointmentController(manager));
        pages.put(Page.SCHEDULE, new ScheduleController(manager));
        pages.put(Page.SETTINGS, new SettingsController(manager));

        content.setTop(buildHeader());
        root.setLeft(buildSidebar());
        root.setCenter(content);

        navigate(Page.DASHBOARD);
        startClock();
    }

    public BorderPane getRoot() { return root; }

    @Override
    public void navigate(Page page) {
        current = page;
        PageController controller = pages.get(page);
        controller.refresh();
        content.setCenter(controller.getView());
        highlight();
    }

    @Override
    public void openSearch() {
        new SearchController(manager).show();
    }

    // ============================ SIDEBAR ============================

    private VBox buildSidebar() {
        Label title = new Label("SmartCare\nHospital");
        title.getStyleClass().add("sidebar-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        Label tagline = new Label("Care Today... Healthier Tomorrow");
        tagline.getStyleClass().add("sidebar-tagline");

        VBox brand = new VBox(8, Logo.heart(84, Color.WHITE, Color.web("#075E60")), title, tagline);
        brand.setAlignment(Pos.CENTER);
        brand.setPadding(new Insets(10, 0, 22, 0));

        VBox menu = new VBox(8, brand);
        menu.getChildren().add(navButton(Page.DASHBOARD, "Dashboard", Icons.HOME));
        menu.getChildren().add(navButton(Page.PATIENTS, "Patient Management", Icons.PEOPLE));
        menu.getChildren().add(navButton(Page.DOCTORS, "Doctor Management", Icons.PERSON));
        menu.getChildren().add(navButton(Page.APPOINTMENTS, "Appointment Management", Icons.CALENDAR));
        menu.getChildren().add(navButton(Page.SCHEDULE, "Daily Schedule", Icons.CLOCK));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Separator line = new Separator();
        line.getStyleClass().add("sidebar-separator");

        Button exit = new Button("Exit");
        exit.getStyleClass().add("nav-button");
        exit.setGraphic(Icons.of(Icons.EXIT, 22, Color.WHITE));
        exit.setGraphicTextGap(14);
        exit.setContentDisplay(ContentDisplay.LEFT);
        exit.setMaxWidth(Double.MAX_VALUE);
        exit.setOnAction(e -> confirmExit());

        VBox sidebar = new VBox(8, menu, spacer, line,
                navButton(Page.SETTINGS, "Settings", Icons.GEAR), exit);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(24, 14, 24, 14));
        sidebar.setPrefWidth(270);
        sidebar.setMinWidth(270);
        return sidebar;
    }

    private Button navButton(Page page, String text, String iconPath) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setGraphicTextGap(14);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setOnAction(e -> navigate(page));
        navButtons.put(page, button);
        navIcons.put(page, iconPath);
        return button;
    }

    private void highlight() {
        for (Map.Entry<Page, Button> entry : navButtons.entrySet()) {
            boolean active = entry.getKey() == current;
            Button button = entry.getValue();
            button.getStyleClass().remove("nav-button-active");
            if (active) {
                button.getStyleClass().add("nav-button-active");
            }
            button.setGraphic(Icons.of(navIcons.get(entry.getKey()), 22, active ? DARK : Color.WHITE));
        }
    }

    private void confirmExit() {
        if (Dialogs.confirm("Do you want to exit SmartCare Hospital?")) {
            Platform.exit();
        }
    }

    // ============================ HEADER ============================

    private HBox buildHeader() {
        Label title = new Label("SmartCare Hospital");
        title.getStyleClass().add("header-title");
        Label subtitle = new Label("Patient and Appointment Manager");
        subtitle.getStyleClass().add("header-subtitle");
        VBox titles = new VBox(0, title, subtitle);
        titles.setAlignment(Pos.CENTER_LEFT);

        dateLabel.getStyleClass().add("datetime-label");
        timeLabel.getStyleClass().add("datetime-label");
        Label divider = new Label("|");
        divider.getStyleClass().add("datetime-label");
        HBox dateTime = new HBox(10, Icons.of(Icons.CALENDAR, 22, Color.web("#075E60")),
                dateLabel, divider, timeLabel);
        dateTime.setAlignment(Pos.CENTER);

        MenuItem role = new MenuItem("Hospital Administrator");
        role.setDisable(true);
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> confirmExit());
        MenuButton admin = new MenuButton("Admin", Icons.of(Icons.PERSON, 22, Color.WHITE),
                role, new SeparatorMenuItem(), exit);
        admin.getStyleClass().add("admin-menu");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(18, Logo.heart(66, Color.web("#008F91"), Color.WHITE), titles,
                Logo.heartbeatLine(300), spacer, dateTime, admin);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(16, 24, 10, 24));
        return header;
    }

    private void startClock() {
        updateClock();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateClock()));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(AppConstants.HEADER_DATE_FORMAT));
        timeLabel.setText(now.format(AppConstants.TIME_FORMAT));
    }
}
