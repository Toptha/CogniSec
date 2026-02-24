# CogniSec - Real-Time Network Threat Monitor

CogniSec is a JavaFX-based desktop application designed for real-time network traffic analysis, process mapping, and autonomous threat detection. It captures raw network packets, correlates them to local running processes, and assesses their threat level using local blacklists, AbuseIPDB intelligence, and MaxMind GeoIP data.

![CogniSec Dashboard Screenshot](screenshot_placeholder.png) *(Note: Add a screenshot of the dashboard here)*

## Features

- **Live Packet Capture:** Utilizes `pcap4j` to monitor live traffic on active network interfaces.
- **Process Correlation:** Maps captured connections back to their origin processes on Windows (using `netstat` and `tasklist`).
- **Threat Intelligence:**
  - Validates destination IPs against dynamic blocklists (e.g., FireHOL Level 1).
  - Integrates with the **AbuseIPDB API** for deeper IP reputation scoring.
  - Automatically escalates and alerts on suspicious traffic.
- **Geolocation tracking:** Uses the offline **MaxMind GeoLite2 City** database to map remote IPs to their respective countries securely without DNS leak.
- **Historical Analysis:** Stores network logs and triggered alerts into a MySQL database for long-term review and correlation.
- **Modern UI:** Built with JavaFX and styled with custom CSS for a premium, dark-themed, glassmorphic experience.

## Prerequisites

Before running CogniSec, ensure you have the following installed:
- **Java Development Kit (JDK) 17+**
- **Apache Maven 3.9+** (or use the provided `.mvnw` wrapper)
- **Npcap / WinPcap (Windows)** or **libpcap (Linux/macOS)** needed by `pcap4j` for raw packet capturing. Npcap is highly recommended for Windows users.
- **MySQL Server 8.x+** running locally.

## Setup & Configuration

### 1. Database Configuration
Ensure MySQL is running. CogniSec relies on a `DatabaseService` to initialize the `threat_monitor` schema and the `network_logs` / `alerts` tables automatically. Check the JDBC URI and credentials inside the codebase (`DatabaseService.java`) to make sure they match your environment.

### 2. AbuseIPDB API Key
CogniSec requires an AbuseIPDB API key for extended threat reputation checks. Ensure the `ABUSEIPDB_KEY` in `ThreatIntelService.java` is updated with your API key to avoid rate limiting.

### 3. MaxMind GeoLite2 Database
The application requires the MaxMind `GeoLite2-City.mmdb` database for IP to Country resolution. 
1. Download it from the [MaxMind website](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data).
2. Place the `GeoLite2-City.mmdb` file directly in the run directory (the root directory where you execute the `mvn` command).

## Running the Application

CogniSec is built using Maven and uses the JavaFX Maven plugin.

To compile and launch the application directly from the root directory, simply run:

```bash
mvn clean javafx:run
```

Wait a moment while the application initializes the UI, loads the blocklist from FireHOL, parses the GeoIP database, and binds to your primary network interface.

> **Note on Windows:** Because CogniSec maps ports to processes using system commands (`netstat -ano` and `tasklist`), it heavily integrates with the Windows host OS. Ensure you are running it in an environment where Java has permission to spawn these subprocesses. Admin (Elevated) privileges may be required to monitor all processes.

## Built With

* [JavaFX 17](https://openjfx.io/) - UI framework
* [Pcap4J](https://github.com/kaitoy/pcap4j) - Packet capturing library
* [MaxMind GeoIP2](https://github.com/maxmind/GeoIP2-java) - IP Geolocation
* [Caffeine](https://github.com/ben-manes/caffeine) - High performance caching
* [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) - Database integration 

## License

This project is intended for educational and local analysis purposes.
