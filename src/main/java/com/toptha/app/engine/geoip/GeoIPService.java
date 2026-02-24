package com.toptha.app.engine.geoip;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.model.CityResponse;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class GeoIPService {

    private DatabaseReader dbReader;
    private final Cache<String, String> countryCache;

    public GeoIPService() {
        // Initialize caffeine cache for performance
        countryCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();

        try {
            // Check if GeoLite2-City.mmdb exists in the data directory
            File database = new File(System.getProperty("user.dir"), "GeoLite2-City.mmdb");
            if (database.exists()) {
                dbReader = new DatabaseReader.Builder(database).build();
                System.out.println("GeoIPService: Successfully loaded MaxMind database.");
            } else {
                System.err.println("GeoIPService: No GeoLite2-City.mmdb found in application directory.");
            }
        } catch (Exception e) {
            System.err.println("GeoIPService error: " + e.getMessage());
        }
    }

    public String getCountry(String ipAddress) {
        if (ipAddress == null || ipAddress.isEmpty()) {
            return "Unknown";
        }

        // Fast local/reserved IPs check
        if (ipAddress.startsWith("10.") || ipAddress.startsWith("192.168.") ||
                ipAddress.startsWith("127.") || ipAddress.startsWith("172.") ||
                ipAddress.startsWith("224.") || ipAddress.startsWith("239.")) {
            return "India";
        }

        return countryCache.get(ipAddress, key -> lookupCountry(key));
    }

    private String lookupCountry(String ipAddress) {
        if (dbReader == null) {
            return "Unknown Database";
        }

        try {
            InetAddress ip = InetAddress.getByName(ipAddress);
            CityResponse response = dbReader.city(ip);

            String country = response.getCountry().getName();
            if (country != null) {
                return country;
            }
        } catch (AddressNotFoundException e) {
            // Expected for some IPs, return Unknown
        } catch (Exception e) {
            System.err.println("GeoIP lookup failed for " + ipAddress + ": " + e.getMessage());
        }
        return "Unknown";
    }
}
