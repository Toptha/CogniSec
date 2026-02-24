public class TestMapper {
    public static void main(String[] args) throws Exception {
        java.lang.Process netstat = Runtime.getRuntime().exec("netstat -ano");
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(netstat.getInputStream()))) {
            String line;
            java.util.regex.Pattern p = java.util.regex.Pattern
                    .compile("^\\s*(TCP|UDP)\\s+\\S+:(\\d+)\\s+.*?\\s+(\\d+)\\s*$");
            while ((line = reader.readLine()) != null) {
                java.util.regex.Matcher m = p.matcher(line);
                if (m.find()) {
                    System.out.println("Match: " + m.group(1) + ":" + m.group(2) + " -> " + m.group(3));
                } else if (line.contains("TCP") || line.contains("UDP")) {
                    System.out.println("No match: " + line);
                }
            }
        }
    }
}
