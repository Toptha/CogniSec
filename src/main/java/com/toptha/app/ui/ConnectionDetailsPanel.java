package com.toptha.app.ui;

import com.toptha.app.engine.firewall.FirewallController;
import com.toptha.app.model.Connection;
import com.toptha.app.model.ProcessInfo;
import com.toptha.app.model.Alert;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConnectionDetailsPanel extends VBox {

    private final Label lblProcess = new Label("-");
    private final Label lblPid = new Label("-");
    private final Label lblPath = new Label("-");
    private final Label lblDstIp = new Label("-");
    private final Label lblCountry = new Label("-");
    private final Label lblAbuseScore = new Label("-");
    private final Label lblPort = new Label("-");
    private final Label lblBytes = new Label("-");
    private final Label lblThreat = new Label("-");
    private final Label lblTime = new Label("-");

    private final Button btnBlockIp = new Button("Block IP");
    private final Button btnBlockProc = new Button("Block Process");
    private final Button btnSuspendProc = new Button("Suspend Process");
    private final Button btnKillProc = new Button("Kill Process");

    private Connection currentConn;
    private final AppController controller;
    private final FirewallController firewall;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ConnectionDetailsPanel(AppController controller) {
        this.controller = controller;
        this.firewall = controller.getFirewall();
        getStyleClass().add("panel");
        setSpacing(10);
        setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Label title = new Label("Connection Details");
        title.getStyleClass().add("panel-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        addDetailRow(grid, 0, "Process:", lblProcess);
        addDetailRow(grid, 1, "PID:", lblPid);
        addDetailRow(grid, 2, "Path:", lblPath);
        addDetailRow(grid, 3, "Remote IP:", lblDstIp);
        addDetailRow(grid, 4, "Country:", lblCountry);
        addDetailRow(grid, 5, "Abuse Score:", lblAbuseScore);
        addDetailRow(grid, 6, "Port:", lblPort);
        addDetailRow(grid, 7, "Traffic:", lblBytes);
        addDetailRow(grid, 8, "Threat:", lblThreat);
        addDetailRow(grid, 9, "First Seen:", lblTime);

        btnBlockIp.getStyleClass().addAll("button", "button-danger");
        btnBlockIp.setOnAction(e -> {
            if (currentConn != null) {
                firewall.blockIp(currentConn.getDstIp());
                currentConn.setBlockedIp(true);
                btnBlockIp.setText("Blocked");
                btnBlockIp.setDisable(true);
                // Fire an alert to provide visual feedback
                String msg = "Manually blocked IP address: " + currentConn.getDstIp();
                Alert alert = new Alert(LocalDateTime.now(), "INFO", msg, "Manual Block");
                Platform.runLater(() -> controller.getAlerts().add(0, alert));
            }
        });

        btnBlockProc.getStyleClass().addAll("button", "button-danger");
        btnBlockProc.setOnAction(e -> {
            if (currentConn != null && currentConn.getProcess() != null) {
                firewall.blockProcess(currentConn.getProcess().getExecutablePath());
                currentConn.setBlockedProcess(true);
                btnBlockProc.setText("Proc Blocked");
                btnBlockProc.setDisable(true);
                // Fire an alert to provide visual feedback
                String msg = "Manually blocked Process: " + currentConn.getProcess().getExecutablePath();
                Alert alert = new Alert(LocalDateTime.now(), "INFO", msg, "Manual Block");
                Platform.runLater(() -> controller.getAlerts().add(0, alert));
            }
        });

        btnSuspendProc.getStyleClass().addAll("button", "button-warning");
        btnSuspendProc.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #fff;");
        btnSuspendProc.setOnAction(e -> {
            if (currentConn != null && currentConn.getProcess() != null) {
                firewall.suspendProcess(currentConn.getProcess().getPid());
                currentConn.setSuspended(true);
                btnSuspendProc.setText("Suspended");
                btnSuspendProc.setDisable(true);
                // Fire an alert to provide visual feedback
                String msg = "Manually suspended Process ID: " + currentConn.getProcess().getPid();
                Alert alert = new Alert(LocalDateTime.now(), "INFO", msg, "Manual Suspend");
                Platform.runLater(() -> controller.getAlerts().add(0, alert));
            }
        });

        btnKillProc.getStyleClass().addAll("button", "button-danger");
        btnKillProc.setOnAction(e -> {
            if (currentConn != null && currentConn.getProcess() != null) {
                firewall.killProcess(currentConn.getProcess().getPid());
                currentConn.setTerminated(true);
                btnKillProc.setText("Terminated");
                btnKillProc.setDisable(true);
                // Fire an alert to provide visual feedback
                String msg = "Manually terminated Process ID: " + currentConn.getProcess().getPid();
                Alert alert = new Alert(LocalDateTime.now(), "INFO", msg, "Manual Kill");
                Platform.runLater(() -> controller.getAlerts().add(0, alert));
            }
        });

        javafx.scene.layout.FlowPane actions = new javafx.scene.layout.FlowPane();
        actions.setHgap(10);
        actions.setVgap(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.getChildren().addAll(btnBlockIp, btnBlockProc, btnSuspendProc, btnKillProc);

        getChildren().addAll(title, grid, actions);
    }

    private void addDetailRow(GridPane grid, int row, String name, Label valueLabel) {
        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: #a1a1aa; -fx-font-weight: bold;");
        valueLabel.setStyle("-fx-text-fill: #f4f4f5;");
        valueLabel.setWrapText(true);

        grid.add(nameLbl, 0, row);
        grid.add(valueLabel, 1, row);
    }

    public void setConnection(Connection conn) {
        if (conn != null && conn != this.currentConn) {
            // Play a slick refresh animation on the panel
            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(250),
                    this);
            ft.setFromValue(0.4);
            ft.setToValue(1.0);

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    javafx.util.Duration.millis(250), this);
            tt.setFromY(-5);
            tt.setToY(0);

            javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
            pt.play();
        }

        this.currentConn = conn;
        if (conn == null) {
            resetFields();
            return;
        }

        ProcessInfo proc = conn.getProcess();
        lblProcess.setText(proc != null ? proc.getName() : "Unknown");
        lblPid.setText(proc != null ? String.valueOf(proc.getPid()) : "0");
        lblPath.setText(proc != null ? proc.getExecutablePath() : "N/A");
        lblDstIp.setText(conn.getDstIp());
        lblCountry.setText(conn.getCountry());

        lblAbuseScore.setText("-");
        lblAbuseScore.setStyle("-fx-text-fill: #f4f4f5;");
        if (!conn.getDstIp().equals("127.0.0.1") && !conn.getDstIp().startsWith("192.168.")) {
            if (conn.getAbuseScore() != -1) {
                applyScoreStyle(conn.getAbuseScore());
            } else {
                lblAbuseScore.setText("Scanning API...");
                lblAbuseScore.setStyle("-fx-text-fill: #a1a1aa; -fx-font-style: italic;");
                // Wait, ThreatDetectionEngine doesn't expose it. We need to add it to
                // AppController.
                controller.getIntelService().getAbuseConfidenceScore(conn.getDstIp()).thenAccept(score -> {
                    Platform.runLater(() -> {
                        if (this.currentConn == conn) { // Ensure they haven't clicked away
                            conn.setAbuseScore(score);
                            applyScoreStyle(score);
                        }
                    });
                });
            }
        } else {
            lblAbuseScore.setText("Local / Safe");
            lblAbuseScore.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        }

        lblPort.setText(conn.getDstPort() + " (" + conn.getProtocol() + ")");
        lblBytes.setText(conn.getTotalBytes() + " bytes");
        lblThreat.setText(conn.getThreatLevel().name());
        lblTime.setText(conn.getStartTime().format(TIME_FORMAT));

        if (conn.isBlockedIp()) {
            btnBlockIp.setDisable(true);
            btnBlockIp.setText("Blocked");
        } else {
            btnBlockIp.setDisable(false);
            btnBlockIp.setText("Block IP");
        }

        if (proc == null) {
            btnBlockProc.setDisable(true);
            btnBlockProc.setText("Block Process");
            btnSuspendProc.setDisable(true);
            btnSuspendProc.setText("Suspend Process");
            btnKillProc.setDisable(true);
            btnKillProc.setText("Kill Process");
        } else {
            if (conn.isBlockedProcess()) {
                btnBlockProc.setDisable(true);
                btnBlockProc.setText("Proc Blocked");
            } else {
                btnBlockProc.setDisable(false);
                btnBlockProc.setText("Block Process");
            }

            if (conn.isSuspended()) {
                btnSuspendProc.setDisable(true);
                btnSuspendProc.setText("Suspended");
            } else {
                btnSuspendProc.setDisable(false);
                btnSuspendProc.setText("Suspend Process");
            }

            if (conn.isTerminated()) {
                btnKillProc.setDisable(true);
                btnKillProc.setText("Terminated");
                btnSuspendProc.setDisable(true); // Can't suspend a dead process
                btnSuspendProc.setText("Suspend Process");
            } else {
                btnKillProc.setDisable(false);
                btnKillProc.setText("Kill Process");
            }
        }
    }

    private void applyScoreStyle(int score) {
        if (score == -1) {
            lblAbuseScore.setText("API Error / Rate Limited");
            lblAbuseScore.setStyle("-fx-text-fill: #ef4444;");
        } else {
            lblAbuseScore.setText(score + "% Confidence of Abuse");
            if (score == 0) {
                lblAbuseScore.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            } else if (score < 50) {
                lblAbuseScore.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            } else {
                lblAbuseScore.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            }
        }
    }

    private void resetFields() {
        lblProcess.setText("-");
        lblPid.setText("-");
        lblPath.setText("-");
        lblDstIp.setText("-");
        lblCountry.setText("-");
        lblPort.setText("-");
        lblBytes.setText("-");
        lblThreat.setText("-");
        lblTime.setText("-");
        btnBlockIp.setDisable(true);
        btnBlockProc.setDisable(true);
        btnSuspendProc.setDisable(true);
        btnKillProc.setDisable(true);
    }
}
