package com.toptha.app.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class DashboardApp extends Application {

    private AppController controller;

    @Override
    public void start(Stage primaryStage) {
        controller = new AppController();
        controller.initialize();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.getStyleClass().add("root");

        // Top Title Bar
        HBox titleBar = new HBox();
        Label appTitle = new Label("CogniSec");
        appTitle.getStyleClass().add("app-title");
        Label subtitle = new Label("Real-Time Threat Monitor");
        subtitle.getStyleClass().add("app-subtitle");
        titleBar.getChildren().addAll(appTitle, subtitle);
        titleBar.setAlignment(Pos.BOTTOM_LEFT);
        titleBar.setSpacing(15);
        titleBar.setPadding(new Insets(0, 0, 20, 0));
        root.setTop(titleBar);

        // --- Tab 1: Live Monitor ---
        BorderPane liveMonitorPane = new BorderPane();

        ConnectionsTable connectionsTable = new ConnectionsTable(controller.getConnections());
        AlertsPanel alertsPanel = new AlertsPanel(controller.getAlerts());
        ConnectionDetailsPanel detailsPanel = new ConnectionDetailsPanel(controller);

        connectionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            detailsPanel.setConnection(newVal);
        });

        liveMonitorPane.setCenter(connectionsTable);

        VBox rightBox = new VBox(20);
        rightBox.getChildren().addAll(alertsPanel, detailsPanel);
        VBox.setVgrow(alertsPanel, Priority.ALWAYS);
        rightBox.setPrefWidth(380);
        BorderPane.setMargin(rightBox, new Insets(0, 0, 0, 20)); // Top, Right, Bottom, Left

        liveMonitorPane.setRight(rightBox);

        Tab liveTab = new Tab("Live Monitor", liveMonitorPane);
        liveTab.setClosable(false);

        // --- Tab 2: Historical Analysis ---
        HistoricalDashboardTab historyTabPane = new HistoricalDashboardTab(controller.getDb(), controller);
        Tab historyTab = new Tab("Historical Analysis", historyTabPane);
        historyTab.setClosable(false);

        // --- Main TabPane ---
        TabPane tabPane = new TabPane(liveTab, historyTab);
        tabPane.getStyleClass().add("main-tab-pane");
        root.setCenter(tabPane);

        // Add sleek transition effect when switching tabs
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() != null) {
                javafx.scene.Node content = newTab.getContent();
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(300), content);
                ft.setFromValue(0.2);
                ft.setToValue(1.0);

                javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                        javafx.util.Duration.millis(300), content);
                tt.setFromY(10);
                tt.setToY(0);

                javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(ft, tt);
                pt.play();
            }
        });

        // Status bar stays at the very bottom of the root
        StatusBar statusBar = new StatusBar(controller);
        root.setBottom(statusBar);
        BorderPane.setMargin(statusBar, new Insets(20, -20, -20, -20)); // To make status bar stretch to bottom edges

        Scene scene = new Scene(root, 1280, 850);

        String css = getClass().getResource("/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("CogniSec - Real-Time Network Threat Monitor");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> controller.shutdown());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
