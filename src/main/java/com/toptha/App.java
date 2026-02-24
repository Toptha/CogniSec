package com.toptha;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class App extends Application {

    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        statusLabel = new Label("Click button to test MySQL JDBC Connection");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);
        
        Button testDbButton = new Button("Test JDBC Connection");
        testDbButton.setStyle("-fx-background-color: #2b3e50; -fx-text-fill: white; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        testDbButton.setOnAction(e -> testConnection());

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f4f4f4; -fx-padding: 30;");
        root.getChildren().addAll(statusLabel, testDbButton);

        Scene scene = new Scene(root, 500, 300);

        primaryStage.setTitle("Real-Time Threat Monitor - Setup Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void testConnection() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/";
        String username = "root";
        String password = "root";

        try {
            // Explicitly force loading the MySQL driver to avoid module initialization issues
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection ignored = DriverManager.getConnection(jdbcUrl, username, password)) {
                statusLabel.setText("Database Connection: SUCCESSFUL!");
                statusLabel.setStyle("-fx-text-fill: #2ecc71;"); // Green
            } catch (SQLException ex) {
                // If it's an auth error or missing DB, it means the driver is working fine and communicating with a DB
                if (ex.getMessage().contains("Access denied") || ex.getMessage().contains("Unknown database")) {
                    statusLabel.setText("Driver works! (Auth/DB error): " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e67e22;"); // Orange
                } else {
                    statusLabel.setText("Database Connection: FAILED!\n" + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red
                }
            }
        } catch (ClassNotFoundException e) {
            statusLabel.setText("JDBC Driver class not found: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
