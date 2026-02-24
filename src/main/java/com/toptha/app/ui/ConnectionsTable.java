package com.toptha.app.ui;

import com.toptha.app.model.Connection;
import com.toptha.app.model.ThreatLevel;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public class ConnectionsTable extends TableView<Connection> {

    public ConnectionsTable(ObservableList<Connection> data) {
        super(data);
        getStyleClass().add("table-view");

        TableColumn<Connection, String> processCol = new TableColumn<>("Process");
        processCol.setCellValueFactory(data1 -> new SimpleStringProperty(
                data1.getValue().getProcess() != null ? data1.getValue().getProcess().getName() : "Unknown"));
        processCol.setPrefWidth(200);

        TableColumn<Connection, Number> pidCol = new TableColumn<>("PID");
        pidCol.setCellValueFactory(data1 -> new SimpleObjectProperty<>(
                data1.getValue().getProcess() != null ? data1.getValue().getProcess().getPid() : 0));
        pidCol.setPrefWidth(90);

        TableColumn<Connection, String> dstIpCol = new TableColumn<>("Destination IP");
        dstIpCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().getDstIp()));
        dstIpCol.setPrefWidth(180);

        TableColumn<Connection, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(data1 -> data1.getValue().countryProperty());
        countryCol.setPrefWidth(180);

        TableColumn<Connection, String> domainCol = new TableColumn<>("Domain");
        domainCol.setCellValueFactory(data1 -> data1.getValue().domainNameProperty());
        domainCol.setPrefWidth(220);

        TableColumn<Connection, Number> portCol = new TableColumn<>("Port");
        portCol.setCellValueFactory(data1 -> new SimpleObjectProperty<>(data1.getValue().getDstPort()));
        portCol.setPrefWidth(90);

        TableColumn<Connection, String> protoCol = new TableColumn<>("Protocol");
        protoCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().getProtocol()));
        protoCol.setPrefWidth(120);

        TableColumn<Connection, String> bytesCol = new TableColumn<>("Traffic");
        bytesCol.setCellValueFactory(data1 -> new SimpleStringProperty(formatBytes(data1.getValue().getTotalBytes())));
        bytesCol.setPrefWidth(120);

        TableColumn<Connection, ThreatLevel> threatCol = new TableColumn<>("Threat Level");
        threatCol.setCellValueFactory(data1 -> data1.getValue().threatLevelProperty());
        threatCol.setPrefWidth(150);

        javafx.css.PseudoClass safeClass = javafx.css.PseudoClass.getPseudoClass("safe");
        javafx.css.PseudoClass suspiciousClass = javafx.css.PseudoClass.getPseudoClass("suspicious");
        javafx.css.PseudoClass threatClass = javafx.css.PseudoClass.getPseudoClass("threat");

        setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(safeClass, false);
                pseudoClassStateChanged(suspiciousClass, false);
                pseudoClassStateChanged(threatClass, false);
                if (item != null && !empty) {
                    switch (item.getThreatLevel()) {
                        case SAFE:
                            pseudoClassStateChanged(safeClass, true);
                            break;
                        case SUSPICIOUS:
                            pseudoClassStateChanged(suspiciousClass, true);
                            break;
                        case THREAT:
                            pseudoClassStateChanged(threatClass, true);
                            break;
                    }
                }
            }
        });

        getColumns().addAll(processCol, pidCol, dstIpCol, countryCol, domainCol, portCol, protoCol, bytesCol,
                threatCol);

        setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
