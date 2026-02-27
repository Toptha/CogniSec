package com.toptha.app.ui;

import com.toptha.app.db.DatabaseService;
import com.toptha.app.model.Alert;
import com.toptha.app.model.Connection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class HistoricalDashboardTab extends BorderPane {

    private final DatabaseService db;
    private final AppController controller;
    private final ObservableList<Connection> historicalData = FXCollections.observableArrayList();
    private ConnectionsTable table;

    public HistoricalDashboardTab(DatabaseService db, AppController controller) {
        this.db = db;
        this.controller = controller;

        setPadding(new Insets(20));

        // Top Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        Label title = new Label("Historical Analysis");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f4f4f5;");

        Button btnRefresh = new Button("Refresh Data");
        btnRefresh.getStyleClass().add("button");
        btnRefresh.setOnAction(e -> loadData());

        Button btnExport = new Button("Export CSV Report");
        btnExport.getStyleClass().add("button");
        btnExport.setOnAction(e -> exportCsv());

        header.getChildren().addAll(title, btnRefresh, btnExport);
        setTop(header);
        BorderPane.setMargin(header, new Insets(0, 0, 20, 0));

        // Center Table
        table = new ConnectionsTable(historicalData);
        setCenter(table);

        // Load Initial Data
        loadData();
    }

    private void loadData() {
        historicalData.clear();
        List<Connection> data = db.getAllConnections();
        historicalData.addAll(data);
    }

    private void exportCsv() {
        File file = new File(System.getProperty("user.home") + "/Desktop/sentinelx_incident_report.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Timestamp,Process,PID,Destination IP,Port,Protocol,Country,Bytes,Threat Level\n");
            for (Connection c : historicalData) {
                String proc = c.getProcess() != null ? c.getProcess().getName() : "Unknown";
                String pid = c.getProcess() != null ? String.valueOf(c.getProcess().getPid()) : "0";
                writer.write(String.format("%s,%s,%s,%s,%d,%s,%s,%d,%s\n",
                        c.getStartTime().toString(),
                        proc,
                        pid,
                        c.getDstIp(),
                        c.getDstPort(),
                        c.getProtocol(),
                        c.getCountry(),
                        c.getTotalBytes(),
                        c.getThreatLevel().name()));
            }

            String msg = "Successfully exported report to Desktop.";
            Alert alert = new Alert(LocalDateTime.now(), "INFO", msg, "Export CSV");
            Platform.runLater(() -> controller.getAlerts().add(0, alert));

        } catch (IOException e) {
            String msg = "Failed to export CSV: " + e.getMessage();
            Alert alert = new Alert(LocalDateTime.now(), "THREAT", msg, "Export Error");
            Platform.runLater(() -> controller.getAlerts().add(0, alert));
        }
    }
}
