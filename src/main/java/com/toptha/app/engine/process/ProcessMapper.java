package com.toptha.app.engine.process;

import com.toptha.app.model.ProcessInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessMapper {

    private long lastUpdate = 0;
    private final Map<String, Integer> portToPid = new HashMap<>();
    private final Map<Integer, String> pidToName = new HashMap<>();

    public synchronized ProcessInfo getProcessForPort(int port, String protocol) {
        if (System.currentTimeMillis() - lastUpdate > 5000) {
            refreshMappings();
        }

        Integer pid = portToPid.get(protocol + ":" + port);
        if (pid != null) {
            String name = pidToName.getOrDefault(pid, "Unknown Process");
            return new ProcessInfo(pid, name, name + ".exe");
        }
        return null;
    }

    private void refreshMappings() {
        portToPid.clear();
        pidToName.clear();
        try {
            // Retrieve port to PID mappings
            Process netstat = Runtime.getRuntime().exec("netstat -ano");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(netstat.getInputStream()))) {
                String line;
                Pattern p = Pattern.compile("^\\s*(TCP|UDP)\\s+\\S+:(\\d+)\\s+.*?\\s+(\\d+)\\s*$");
                while ((line = reader.readLine()) != null) {
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        String proto = m.group(1);
                        int port = Integer.parseInt(m.group(2));
                        int pid = Integer.parseInt(m.group(3));
                        if (pid > 0)
                            portToPid.put(proto + ":" + port, pid);
                    }
                }
            }

            // Retrieve PID to Name mappings
            Process tasklist = Runtime.getRuntime().exec("tasklist /fo csv /nh");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(tasklist.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\",\"");
                    if (parts.length >= 2) {
                        String name = parts[0].replace("\"", "");
                        int pid = Integer.parseInt(parts[1].replace("\"", ""));
                        pidToName.put(pid, name);
                    }
                }
            }
            lastUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            System.err.println("ProcessMapper error: " + e.getMessage());
        }
    }
}
