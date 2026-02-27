package com.toptha.app.ui;

import com.toptha.app.model.ThreatLevel;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class StatusBar extends HBox {

    private final Label lblStatus;

    public StatusBar(AppController controller) {
        getStyleClass().add("status-bar");
        setPadding(new javafx.geometry.Insets(10, 15, 10, 15));
        setSpacing(20);
        setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        lblStatus = new javafx.scene.control.Label("Initializing capture engine...");
        lblStatus.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 14px; -fx-font-weight: bold;");

        getChildren().add(lblStatus);

        Thread monitor = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    int total = controller.getConnections().size();
                    long threats = controller.getConnections().stream()
                            .filter(c -> c.getThreatLevel() == ThreatLevel.THREAT)
                            .count();

                    Platform.runLater(() -> {
                        lblStatus.setText(
                                String.format("Capture: ACTIVE | Connections: %d | Threats: %d | System Secured",
                                        total, threats));
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
}
