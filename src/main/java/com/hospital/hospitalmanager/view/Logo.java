package com.hospital.hospitalmanager.view;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineJoin;

/** The hospital logo and small illustrations, all built from JavaFX shapes. */
public final class Logo {
    private Logo() { }

    public static final Color TEAL_DARK = Color.web("#075E60");
    public static final Color TEAL = Color.web("#008F91");
    public static final Color AQUA = Color.web("#B8EEEE");

    /** A heart with a white cross: the SmartCare logo. */
    public static Node heart(double size, Color heartColor, Color crossColor) {
        Node heart = Icons.of(Icons.HEART, size, heartColor);
        double arm = size * 0.46;
        double thick = size * 0.13;
        Rectangle horizontal = new Rectangle(arm, thick, crossColor);
        Rectangle vertical = new Rectangle(thick, arm, crossColor);
        horizontal.setArcWidth(thick / 2);
        horizontal.setArcHeight(thick / 2);
        vertical.setArcWidth(thick / 2);
        vertical.setArcHeight(thick / 2);
        StackPane cross = new StackPane(horizontal, vertical);
        cross.setMaxSize(arm, arm);
        cross.setTranslateY(-size * 0.03);
        StackPane logo = new StackPane(heart, cross);
        logo.setMinSize(size, size);
        logo.setPrefSize(size, size);
        logo.setMaxSize(size, size);
        return logo;
    }

    /** A heartbeat (ECG) line used in the header. */
    public static Node heartbeatLine(double width) {
        double mid = 22;
        double start = width * 0.30;
        Polyline line = new Polyline(
                0, mid, start, mid, start + 14, mid, start + 24, 4, start + 38, 40,
                start + 50, 12, start + 60, mid, width, mid);
        line.setStroke(TEAL);
        line.setStrokeWidth(2.5);
        line.setStrokeLineJoin(StrokeLineJoin.ROUND);
        Pane holder = new Pane(line);
        holder.setMinSize(width, 44);
        holder.setPrefSize(width, 44);
        holder.setMaxSize(width, 44);
        return holder;
    }

    /** A small hospital building with trees. */
    public static Node building() {
        Pane p = new Pane();
        p.setMinSize(190, 125);
        p.setPrefSize(190, 125);
        p.setMaxSize(190, 125);

        p.getChildren().add(rect(0, 112, 190, 8, Color.web("#CDEBE6"), null, 4));
        p.getChildren().add(rect(40, 40, 110, 72, Color.WHITE, TEAL_DARK, 4));
        p.getChildren().add(rect(72, 18, 46, 26, Color.WHITE, TEAL_DARK, 4));
        p.getChildren().add(rect(91, 22, 8, 18, TEAL, null, 2));
        p.getChildren().add(rect(86, 27, 18, 8, TEAL, null, 2));

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                p.getChildren().add(rect(50 + col * 25, 50 + row * 24, 15, 15, Color.web("#7FD1E8"), TEAL_DARK, 2));
            }
        }
        p.getChildren().add(rect(85, 90, 20, 22, TEAL, null, 3));

        p.getChildren().add(rect(16, 90, 6, 22, Color.web("#8B6B4A"), null, 2));
        p.getChildren().add(circle(19, 80, 16, Color.web("#3FA35B")));
        p.getChildren().add(rect(166, 92, 6, 20, Color.web("#8B6B4A"), null, 2));
        p.getChildren().add(circle(169, 82, 14, Color.web("#3FA35B")));
        return p;
    }

    private static Rectangle rect(double x, double y, double w, double h, Color fill, Color stroke, double arc) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        if (stroke != null) {
            r.setStroke(stroke);
            r.setStrokeWidth(1.5);
        }
        r.setArcWidth(arc);
        r.setArcHeight(arc);
        return r;
    }

    private static Circle circle(double x, double y, double radius, Color fill) {
        Circle c = new Circle(x, y, radius);
        c.setFill(fill);
        return c;
    }
}
