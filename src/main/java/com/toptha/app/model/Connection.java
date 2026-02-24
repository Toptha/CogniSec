package com.toptha.app.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Connection {
    // Immutable properties identifying the connection
    private final String srcIp;
    private final String dstIp;
    private final int srcPort;
    private final int dstPort;
    private final String protocol;

    // Mutable properties (JavaFX properties for UI updates)
    private final LongProperty bytesSent = new SimpleLongProperty(0);
    private final LongProperty bytesReceived = new SimpleLongProperty(0);
    private final ObjectProperty<LocalDateTime> startTime = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lastSeen = new SimpleObjectProperty<>();
    private final ObjectProperty<ProcessInfo> process = new SimpleObjectProperty<>();
    private final ObjectProperty<ThreatLevel> threatLevel = new SimpleObjectProperty<>(ThreatLevel.INFO);
    private final StringProperty country = new SimpleStringProperty("Unknown");
    private final BooleanProperty blockedIp = new SimpleBooleanProperty(false);
    private final BooleanProperty blockedProcess = new SimpleBooleanProperty(false);
    private final BooleanProperty terminated = new SimpleBooleanProperty(false);
    private final BooleanProperty suspended = new SimpleBooleanProperty(false);
    private final StringProperty domainName = new SimpleStringProperty("Resolving...");
    private final IntegerProperty abuseScore = new SimpleIntegerProperty(-1);

    public Connection(String srcIp, String dstIp, int srcPort, int dstPort, String protocol) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
        LocalDateTime now = LocalDateTime.now();
        this.startTime.set(now);
        this.lastSeen.set(now);
    }

    public String getSrcIp() {
        return srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public long getBytesSent() {
        return bytesSent.get();
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent.set(bytesSent);
    }

    public void addBytesSent(long bytes) {
        this.bytesSent.set(this.bytesSent.get() + bytes);
    }

    public LongProperty bytesSentProperty() {
        return bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived.get();
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived.set(bytesReceived);
    }

    public void addBytesReceived(long bytes) {
        this.bytesReceived.set(this.bytesReceived.get() + bytes);
    }

    public LongProperty bytesReceivedProperty() {
        return bytesReceived;
    }

    public long getTotalBytes() {
        return getBytesSent() + getBytesReceived();
    }

    public LocalDateTime getStartTime() {
        return startTime.get();
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime.set(startTime);
    }

    public ObjectProperty<LocalDateTime> startTimeProperty() {
        return startTime;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen.get();
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen.set(lastSeen);
    }

    public ObjectProperty<LocalDateTime> lastSeenProperty() {
        return lastSeen;
    }

    public ProcessInfo getProcess() {
        return process.get();
    }

    public void setProcess(ProcessInfo process) {
        this.process.set(process);
    }

    public ObjectProperty<ProcessInfo> processProperty() {
        return process;
    }

    public ThreatLevel getThreatLevel() {
        return threatLevel.get();
    }

    public void setThreatLevel(ThreatLevel threatLevel) {
        this.threatLevel.set(threatLevel);
    }

    public ObjectProperty<ThreatLevel> threatLevelProperty() {
        return threatLevel;
    }

    public String getCountry() {
        return country.get();
    }

    public void setCountry(String country) {
        this.country.set(country);
    }

    public StringProperty countryProperty() {
        return country;
    }

    public boolean isBlockedIp() {
        return blockedIp.get();
    }

    public void setBlockedIp(boolean blockedIp) {
        this.blockedIp.set(blockedIp);
    }

    public BooleanProperty blockedIpProperty() {
        return blockedIp;
    }

    public boolean isBlockedProcess() {
        return blockedProcess.get();
    }

    public void setBlockedProcess(boolean blockedProcess) {
        this.blockedProcess.set(blockedProcess);
    }

    public BooleanProperty blockedProcessProperty() {
        return blockedProcess;
    }

    public boolean isTerminated() {
        return terminated.get();
    }

    public void setTerminated(boolean terminated) {
        this.terminated.set(terminated);
    }

    public BooleanProperty terminatedProperty() {
        return terminated;
    }

    public boolean isSuspended() {
        return suspended.get();
    }

    public void setSuspended(boolean suspended) {
        this.suspended.set(suspended);
    }

    public BooleanProperty suspendedProperty() {
        return suspended;
    }

    public String getDomainName() {
        return domainName.get();
    }

    public void setDomainName(String domainName) {
        this.domainName.set(domainName);
    }

    public StringProperty domainNameProperty() {
        return domainName;
    }

    public int getAbuseScore() {
        return abuseScore.get();
    }

    public void setAbuseScore(int abuseScore) {
        this.abuseScore.set(abuseScore);
    }

    public IntegerProperty abuseScoreProperty() {
        return abuseScore;
    }

    // Use a composite key, assuming src is local and dst is remote.
    // ConnectionTracker should normalize this.
    public String getConnectionKey() {
        return protocol + ":" + srcIp + ":" + srcPort + "->" + dstIp + ":" + dstPort;
    }
}
