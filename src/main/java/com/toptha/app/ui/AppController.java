package com.toptha.app.ui;

import com.toptha.app.db.DatabaseService;
import com.toptha.app.engine.capture.PacketCaptureService;
import com.toptha.app.engine.connection.ConnectionTracker;
import com.toptha.app.engine.firewall.FirewallController;
import com.toptha.app.engine.process.ProcessMapper;
import com.toptha.app.engine.threat.ThreatDetectionEngine;
import com.toptha.app.engine.threat.ThreatIntelService;
import com.toptha.app.model.Alert;
import com.toptha.app.model.Connection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AppController {

    private final ObservableList<Connection> connections = FXCollections.observableArrayList();
    private final ObservableList<Alert> alerts = FXCollections.observableArrayList();

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private PacketCaptureService captureService;
    private DatabaseService dbService;
    private FirewallController firewall;
    private ThreatIntelService intelService;

    public void initialize() {
        System.out.println("Initializing AppController...");

        dbService = new DatabaseService();
        firewall = new FirewallController();

        ProcessMapper mapper = new ProcessMapper();
        intelService = new ThreatIntelService();
        ThreatDetectionEngine engine = new ThreatDetectionEngine(intelService);

        Consumer<Alert> alertHandler = alert -> Platform.runLater(() -> alerts.add(0, alert));

        ConnectionTracker tracker = new ConnectionTracker(connections, mapper, intelService, engine, dbService,
                firewall,
                alertHandler);

        captureService = new PacketCaptureService(tracker::handlePacket);
        executor.submit(captureService);
    }

    public void shutdown() {
        if (captureService != null) {
            captureService.stop();
        }
        executor.shutdownNow();
    }

    public ObservableList<Connection> getConnections() {
        return connections;
    }

    public ObservableList<Alert> getAlerts() {
        return alerts;
    }

    public FirewallController getFirewall() {
        return firewall;
    }

    public DatabaseService getDb() {
        return dbService;
    }

    public ThreatIntelService getIntelService() {
        return intelService;
    }
}
