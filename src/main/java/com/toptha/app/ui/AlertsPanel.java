package com.toptha.app.ui;

import com.toptha.app.model.Alert;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.format.DateTimeFormatter;

public class AlertsPanel extends VBox {

    private final VBox alertsBox;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AlertsPanel(ObservableList<Alert> alerts) {
        getStyleClass().add("panel");
        setSpacing(10);
        setPrefHeight(400);

        Label title = new Label("Alerts Feed");
        title.getStyleClass().add("panel-title");

        alertsBox = new VBox(5);
        alertsBox.setPadding(new Insets(5));
        alertsBox.setMaxHeight(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(alertsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(title, scrollPane);

        alerts.addListener((ListChangeListener<Alert>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Alert alert : c.getAddedSubList()) {
                        addAlert(alert);
                    }
                }
            }
        });

        for (Alert a : alerts)
            addAlert(a);
    }

    private void addAlert(Alert alert) {
        VBox box = new VBox(2);
        box.getStyleClass().add("alert-box");

        Text timeLbl = new Text(alert.getTimestamp().format(TIME_FORMAT) + " - " + alert.getType());
        timeLbl.setStyle(
                "-fx-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: 600; -fx-font-family: 'JetBrains Mono';");

        if ("THREAT".equals(alert.getType())) {
            box.getStyleClass().add("alert-box-threat");
            timeLbl.setStyle(
                    "-fx-fill: #ef4444; -fx-font-size: 13px; -fx-font-weight: bold; -fx-font-family: 'JetBrains Mono';");

            // Pulsing animation for Threat boxes to make it obvious
            javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
            glow.setColor(javafx.scene.paint.Color.web("#ef4444"));
            glow.setRadius(15);
            glow.setSpread(0.2);
            box.setEffect(glow);

            javafx.animation.Timeline pulse = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                            new javafx.animation.KeyValue(glow.radiusProperty(), 15),
                            new javafx.animation.KeyValue(glow.colorProperty(),
                                    javafx.scene.paint.Color.web("#ef4444", 0.5))),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(800),
                            new javafx.animation.KeyValue(glow.radiusProperty(), 30),
                            new javafx.animation.KeyValue(glow.colorProperty(),
                                    javafx.scene.paint.Color.web("#ef4444", 0.9))));
            pulse.setAutoReverse(true);
            pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
            pulse.play();
        }

        Text msg = new Text(alert.getMessage());
        msg.setStyle("-fx-fill: #d4d4d8; -fx-font-size: 13px; -fx-font-family: 'Inter';");

        TextFlow msgFlow = new TextFlow(msg);
        msgFlow.setMaxWidth(Double.MAX_VALUE);
        msgFlow.prefWidthProperty().bind(alertsBox.widthProperty().subtract(45));

        box.getChildren().addAll(timeLbl, msgFlow);
        box.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        // Initial state for animation
        box.setOpacity(0);
        box.setTranslateX(50); // slide in from right
        alertsBox.getChildren().add(0, box);

        // Entry Animations
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), box);
        ft.setToValue(1.0);

        javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                javafx.util.Duration.millis(400), box);
        tt.setToX(0);
        // Add a slight ease out bounce effect for polish
        tt.setInterpolator(javafx.animation.Interpolator.SPLINE(0.25, 0.1, 0.25, 1));

        javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
        pt.play();

        if (alertsBox.getChildren().size() > 50) {
            alertsBox.getChildren().remove(50, alertsBox.getChildren().size());
        }
    }
}
