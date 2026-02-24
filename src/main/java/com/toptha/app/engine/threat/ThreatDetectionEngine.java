package com.toptha.app.engine.threat;

import com.toptha.app.model.Connection;
import com.toptha.app.model.ThreatLevel;
import com.toptha.app.model.ThreatResult;

public class ThreatDetectionEngine {

    private final ThreatIntelService intelService;

    public ThreatDetectionEngine(ThreatIntelService intelService) {
        this.intelService = intelService;
    }

    public ThreatResult evaluate(Connection connection) {
        String ip = connection.getDstIp();
        String country = connection.getCountry();
        int dstPort = connection.getDstPort();

        if (intelService.isBlacklisted(ip)) {
            return new ThreatResult(ThreatLevel.THREAT, "Known malicious IP Blacklist");
        }

        if ("Russia".equals(country) || "China".equals(country) || "North Korea".equals(country)) {
            return new ThreatResult(ThreatLevel.SUSPICIOUS, "Suspicious high-risk country");
        }

        if (dstPort == 4444 || dstPort == 6667 || dstPort == 31337) {
            return new ThreatResult(ThreatLevel.THREAT, "Suspicious port associated with malware/C2");
        }

        if (dstPort == 3389 && connection.getBytesSent() > 5000000) {
            return new ThreatResult(ThreatLevel.SUSPICIOUS, "High outbound RDP traffic");
        }

        if (connection.getProcess() != null && "powershell.exe".equalsIgnoreCase(connection.getProcess().getName())) {
            return new ThreatResult(ThreatLevel.SUSPICIOUS, "PowerShell initiating network connection");
        }

        if (connection.getBytesSent() > 50000000) { // 50 MB
            return new ThreatResult(ThreatLevel.INFO,
                    "Large data transfer (" + (connection.getBytesSent() / 1000000) + " MB)");
        }

        return new ThreatResult(ThreatLevel.SAFE, "No anomalies detected");
    }
}
