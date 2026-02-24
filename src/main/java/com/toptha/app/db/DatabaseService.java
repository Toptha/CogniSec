package com.toptha.app.db;

import com.toptha.app.model.Alert;
import com.toptha.app.model.Connection;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseService {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "sentinelx";
    private static final String URL = DEFAULT_URL + DB_NAME + "?createDatabaseIfNotExist=true";
    private static final String USER = "root";
    private static final String PASS = "root";

    private java.sql.Connection conn;

    public DatabaseService() {
        connect();
        initializeSchema();
    }

    private void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("DatabaseService: Connected to MySQL database '" + DB_NAME + "'.");
        } catch (Exception e) {
            System.err.println("DatabaseService: Failed to connect or create DB - " + e.getMessage());
        }
    }

    private void initializeSchema() {
        if (conn == null)
            return;

        String createConnections = """
                CREATE TABLE IF NOT EXISTS connections (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    timestamp DATETIME,
                    process VARCHAR(255),
                    pid INT,
                    dst_ip VARCHAR(45),
                    port INT,
                    protocol VARCHAR(10),
                    country VARCHAR(100),
                    bytes BIGINT,
                    threat_level VARCHAR(20)
                )
                """;

        String createAlerts = """
                CREATE TABLE IF NOT EXISTS alerts (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    timestamp DATETIME,
                    type VARCHAR(50),
                    message TEXT,
                    action_taken VARCHAR(100)
                )
                """;

        String createBlockedIps = """
                CREATE TABLE IF NOT EXISTS blocked_ips (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    ip VARCHAR(45),
                    blocked_at DATETIME
                )
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createConnections);
            stmt.execute(createAlerts);
            stmt.execute(createBlockedIps);
            System.out.println("DatabaseService: Schema initialized.");
        } catch (SQLException e) {
            System.err.println("DatabaseService: Failed to initialize schema - " + e.getMessage());
        }
    }

    public synchronized void insertConnection(Connection c) {
        if (conn == null)
            return;
        String sql = "INSERT INTO connections (timestamp, process, pid, dst_ip, port, protocol, country, bytes, threat_level) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, c.getProcess() != null ? c.getProcess().getName() : "Unknown");
            pstmt.setInt(3, c.getProcess() != null ? c.getProcess().getPid() : 0);
            pstmt.setString(4, c.getDstIp());
            pstmt.setInt(5, c.getDstPort());
            pstmt.setString(6, c.getProtocol());
            pstmt.setString(7, c.getCountry());
            pstmt.setLong(8, c.getTotalBytes());
            pstmt.setString(9, c.getThreatLevel().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DatabaseService: Failed to insert connection - " + e.getMessage());
        }
    }

    public synchronized void insertAlert(Alert a) {
        if (conn == null)
            return;
        String sql = "INSERT INTO alerts (timestamp, type, message, action_taken) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(a.getTimestamp()));
            pstmt.setString(2, a.getType());
            pstmt.setString(3, a.getMessage());
            pstmt.setString(4, a.getActionTaken());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    a.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseService: Failed to insert alert - " + e.getMessage());
        }
    }

    public synchronized void insertBlockedIp(String ip) {
        if (conn == null)
            return;
        String sql = "INSERT INTO blocked_ips (ip, blocked_at) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("DatabaseService: Failed to insert blocked IP - " + e.getMessage());
        }
    }

    public synchronized java.util.List<Connection> getAllConnections() {
        java.util.List<Connection> list = new java.util.ArrayList<>();
        if (conn == null)
            return list;

        String sql = "SELECT * FROM connections ORDER BY timestamp DESC LIMIT 500";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Connection c = new Connection(
                        "0.0.0.0", // Dummy SRC IP (historic captures might not strictly store this yet)
                        rs.getString("dst_ip"),
                        0, // Dummy SRC Port
                        rs.getInt("port"),
                        rs.getString("protocol"));

                String processName = rs.getString("process");
                int pid = rs.getInt("pid");
                if (processName != null && !processName.equals("Unknown")) {
                    com.toptha.app.model.ProcessInfo pInfo = new com.toptha.app.model.ProcessInfo(pid, processName,
                            "Historic Process");
                    c.setProcess(pInfo);
                }

                c.setCountry(rs.getString("country"));
                c.addBytesSent(rs.getLong("bytes"));
                c.setStartTime(rs.getTimestamp("timestamp").toLocalDateTime());

                try {
                    c.setThreatLevel(com.toptha.app.model.ThreatLevel.valueOf(rs.getString("threat_level")));
                } catch (Exception ex) {
                    c.setThreatLevel(com.toptha.app.model.ThreatLevel.SAFE);
                }

                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("DatabaseService: Failed to get connections - " + e.getMessage());
        }
        return list;
    }
}
