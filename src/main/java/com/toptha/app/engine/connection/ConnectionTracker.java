package com.toptha.app.engine.connection;

import com.toptha.app.db.DatabaseService;
import com.toptha.app.engine.firewall.FirewallController;
import com.toptha.app.engine.process.ProcessMapper;
import com.toptha.app.engine.threat.ThreatDetectionEngine;
import com.toptha.app.engine.threat.ThreatIntelService;
import com.toptha.app.model.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ConnectionTracker {

    private final ConcurrentHashMap<String, Connection> connectionMap = new ConcurrentHashMap<>();
    private final ObservableList<Connection> uiList;
    private final ProcessMapper processMapper;
    private final ThreatIntelService intelService;
    private final ThreatDetectionEngine detectionEngine;
    private final DatabaseService dbProvider;
    private final FirewallController firewall;
    private final Consumer<Alert> alertListener;

    public ConnectionTracker(ObservableList<Connection> uiList, ProcessMapper processMapper,
            ThreatIntelService intelService, ThreatDetectionEngine detectionEngine,
            DatabaseService dbProvider, FirewallController firewall,
            Consumer<Alert> alertListener) {
        this.uiList = uiList;
        this.processMapper = processMapper;
        this.intelService = intelService;
        this.detectionEngine = detectionEngine;
        this.dbProvider = dbProvider;
        this.firewall = firewall;
        this.alertListener = alertListener;
    }

    public void handlePacket(PacketEvent event) {
        String localIp = isLocal(event.srcIp()) ? event.srcIp() : event.dstIp();
        String remoteIp = isLocal(event.srcIp()) ? event.dstIp() : event.srcIp();

        int localPort = isLocal(event.srcIp()) ? event.srcPort() : event.dstPort();
        int remotePort = isLocal(event.srcIp()) ? event.dstPort() : event.srcPort();

        boolean isUpload = isLocal(event.srcIp());

        String key = event.protocol() + ":" + localIp + ":" + localPort + "->" + remoteIp + ":" + remotePort;

        Connection conn = connectionMap.computeIfAbsent(key, k -> {
            Connection newConn = new Connection(localIp, remoteIp, localPort, remotePort, event.protocol());

            intelService.enrichConnection(newConn);

            ProcessInfo proc = processMapper.getProcessForPort(localPort, event.protocol());
            if (proc != null) {
                newConn.setProcess(proc);
            }

            ThreatResult result = detectionEngine.evaluate(newConn);
            newConn.setThreatLevel(result.level());

            Platform.runLater(() -> uiList.add(newConn));

            if (dbProvider != null) {
                dbProvider.insertConnection(newConn);
            }

            if (result.level() == ThreatLevel.THREAT) {
                triggerAlert(newConn, result.reason());
            }

            return newConn;
        });

        long size = event.size();
        Platform.runLater(() -> {
            if (isUpload) {
                conn.addBytesSent(size);
            } else {
                conn.addBytesReceived(size);
            }
            conn.setLastSeen(LocalDateTime.now());

            if (conn.getTotalBytes() % 1000000 < size) {
                ThreatResult result = detectionEngine.evaluate(conn);
                if (result.level() != conn.getThreatLevel()) {
                    conn.setThreatLevel(result.level());
                    if (result.level() == ThreatLevel.THREAT) {
                        triggerAlert(conn, "Escalated to THREAT: " + result.reason());
                    }
                }
            }
        });
    }

    private boolean isLocal(String ip) {
        return ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("127.") || ip.startsWith("172.");
    }

    private void triggerAlert(Connection conn, String reason) {
        String msg = String.format("Threat detected on %s:%d -> %s. Reason: %s", conn.getProtocol(), conn.getDstPort(),
                conn.getProcess(), reason);
        Alert alert = new Alert(LocalDateTime.now(), "THREAT", msg, "None");

        if (alertListener != null) {
            Platform.runLater(() -> alertListener.accept(alert));
        }

        if (dbProvider != null) {
            dbProvider.insertAlert(alert);
        }
    }
}
