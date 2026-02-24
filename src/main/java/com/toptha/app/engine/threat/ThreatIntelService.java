package com.toptha.app.engine.threat;

import com.toptha.app.model.Connection;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

import com.toptha.app.engine.geoip.GeoIPService;

public class ThreatIntelService {

    private final Set<String> blacklistedIps = new HashSet<>();
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private static final String FIREHOL_URL = "https://raw.githubusercontent.com/firehol/blocklist-ipsets/master/firehol_level1.netset";
    private final GeoIPService geoIPService;

    public ThreatIntelService() {
        this.geoIPService = new GeoIPService();
        downloadLiveBlocklist();
    }

    private void downloadLiveBlocklist() {
        System.out.println("ThreatIntelService: Downloading live blocklist from " + FIREHOL_URL);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(FIREHOL_URL))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        String body = response.body();
                        String[] lines = body.split("\n");
                        int count = 0;
                        for (String line : lines) {
                            line = line.trim();
                            if (!line.isEmpty() && !line.startsWith("#")) {
                                // Add support for exact IPs only for now to keep it simple, strip CIDR if
                                // present
                                if (line.contains("/")) {
                                    blacklistedIps.add(line.substring(0, line.indexOf('/')));
                                } else {
                                    blacklistedIps.add(line);
                                }
                                count++;
                            }
                        }
                        System.out.println("ThreatIntelService: Successfully loaded " + count + " blacklisted IPs.");
                    } else {
                        System.err.println(
                                "ThreatIntelService: Failed to download blocklist, HTTP " + response.statusCode());
                        addFallbackBlacklist();
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("ThreatIntelService: Error downloading blocklist: " + ex.getMessage());
                    addFallbackBlacklist();
                    return null;
                });
    }

    private void addFallbackBlacklist() {
        blacklistedIps.add("185.15.54.2");
        blacklistedIps.add("194.22.1.5");
        blacklistedIps.add("103.45.1.1");
    }

    public void enrichConnection(Connection connection) {
        String ip = connection.getDstIp();

        // Perform asynchronous DNS resolution so we don't block the packet capture loop
        if ("Resolving...".equals(connection.getDomainName())) {
            CompletableFuture.runAsync(() -> {
                try {
                    InetAddress addr = InetAddress.getByName(ip);
                    String host = addr.getHostName();
                    // If it couldn't resolve, it usually just returns the IP back
                    if (host.equals(ip)) {
                        host = "Unknown Host";
                    }
                    final String finalHost = host;
                    Platform.runLater(() -> connection.setDomainName(finalHost));
                } catch (Exception e) {
                    Platform.runLater(() -> connection.setDomainName("Unknown Host"));
                }
            });
        }

        // Use GeoIPService for accurate country resolution without blocking main thread
        CompletableFuture.runAsync(() -> {
            String country = geoIPService.getCountry(ip);
            Platform.runLater(() -> connection.setCountry(country));
        });
    }

    public boolean isBlacklisted(String ip) {
        // Quick lookup in our HashSet
        return blacklistedIps.contains(ip);
    }

    // --- AbuseIPDB Integration ---
    private static final String ABUSEIPDB_KEY = "835a33e9981fa1c07546c1ce05097b24203f0a1cdda90abf4e8a44d80c52378f2f5991736c04bd51";
    private final java.util.Map<String, Integer> abuseCache = new java.util.concurrent.ConcurrentHashMap<>();

    public CompletableFuture<Integer> getAbuseConfidenceScore(String ip) {
        // If it's a local or loopback IP, return 0 immediately
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.") || ip.startsWith("172.")) {
            return CompletableFuture.completedFuture(0);
        }

        // Return cached score if we've already looked up this IP during this session
        if (abuseCache.containsKey(ip)) {
            return CompletableFuture.completedFuture(abuseCache.get(ip));
        }

        String url = "https://api.abuseipdb.com/api/v2/check?ipAddress=" + ip;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Key", ABUSEIPDB_KEY)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        String body = response.body();
                        // Lightweight parsing: looking for "abuseConfidenceScore": 52
                        String target = "\"abuseConfidenceScore\":";
                        int index = body.indexOf(target);
                        if (index != -1) {
                            int start = index + target.length();
                            int end = body.indexOf(",", start);
                            if (end == -1)
                                end = body.indexOf("}", start); // in case it's the last property

                            if (end != -1) {
                                try {
                                    String scoreStr = body.substring(start, end).trim();
                                    int score = Integer.parseInt(scoreStr);
                                    abuseCache.put(ip, score);
                                    return score;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    } else if (response.statusCode() == 429) {
                        System.err.println("AbuseIPDB Rate Limit Exceeded!");
                    } else {
                        System.err.println("AbuseIPDB Request Failed: " + response.statusCode());
                    }
                    return -1; // -1 means failed or could not parse
                });
    }
}
