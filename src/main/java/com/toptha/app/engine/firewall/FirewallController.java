package com.toptha.app.engine.firewall;

import java.io.IOException;

public class FirewallController {

    public void blockIp(String ip) {
        String ruleName = "CogniSec_Block_" + ip;
        // Launch a single PowerShell command that requests admin privileges to add the
        // rules
        String script = String.format(
                "Start-Process powershell -Verb RunAs -WindowStyle Hidden -ArgumentList '-Command \"netsh advfirewall firewall add rule name=''%s'' dir=in action=block remoteip=%s; netsh advfirewall firewall add rule name=''%s_out'' dir=out action=block remoteip=%s\"'",
                ruleName, ip, ruleName, ip);

        try {
            Runtime.getRuntime().exec(new String[] { "powershell", "-Command", script });
            System.out.println("FirewallController: Blocked IP " + ip);
        } catch (IOException e) {
            System.err.println("FirewallController failed to block IP: " + e.getMessage());
        }
    }

    public void unblockIp(String ip) {
        String ruleName = "CogniSec_Block_" + ip;
        String script = String.format(
                "Start-Process powershell -Verb RunAs -WindowStyle Hidden -ArgumentList '-Command \"netsh advfirewall firewall delete rule name=''%s''; netsh advfirewall firewall delete rule name=''%s_out''\"'",
                ruleName, ruleName);

        try {
            Runtime.getRuntime().exec(new String[] { "powershell", "-Command", script });
            System.out.println("FirewallController: Unblocked IP " + ip);
        } catch (IOException e) {
            System.err.println("FirewallController failed to unblock IP: " + e.getMessage());
        }
    }

    public void blockProcess(String exePath) {
        if (exePath == null || exePath.isEmpty())
            return;
        String sanitize = exePath.replaceAll("[^a-zA-Z0-9.-]", "_");
        String ruleName = "CogniSec_Block_Proc_" + sanitize;

        String script = String.format(
                "Start-Process powershell -Verb RunAs -WindowStyle Hidden -ArgumentList '-Command \"netsh advfirewall firewall add rule name=''%s'' dir=out action=block program=''%s''\"'",
                ruleName, exePath);

        try {
            Runtime.getRuntime().exec(new String[] { "powershell", "-Command", script });
            System.out.println("FirewallController: Blocked Process " + exePath);
        } catch (IOException e) {
            System.err.println("FirewallController failed to block process: " + e.getMessage());
        }
    }

    public void killProcess(int pid) {
        if (pid <= 0)
            return;

        String script = String.format(
                "Start-Process powershell -Verb RunAs -WindowStyle Hidden -ArgumentList '-Command \"Stop-Process -Id %d -Force\"'",
                pid);

        try {
            Runtime.getRuntime().exec(new String[] { "powershell", "-Command", script });
            System.out.println("FirewallController: Terminated Process ID " + pid);
        } catch (IOException e) {
            System.err.println("FirewallController failed to terminate process: " + e.getMessage());
        }
    }

    public void suspendProcess(int pid) {
        if (pid <= 0)
            return;

        // Native Windows does not have a "suspend" command line tool, so we compile a
        // tiny C# snippet on the fly using PowerShell
        // that calls the undocumented NtSuspendProcess native API.
        String script = "$c = @'\n" +
                "using System;\n" +
                "using System.Runtime.InteropServices;\n" +
                "public class Native {\n" +
                "    [DllImport(\"ntdll.dll\")]\n" +
                "    public static extern int NtSuspendProcess(IntPtr processHandle);\n" +
                "}\n" +
                "'@\n" +
                "Add-Type -TypeDefinition $c\n" +
                "$proc = Get-Process -Id " + pid + " -ErrorAction SilentlyContinue\n" +
                "if ($proc) { [Native]::NtSuspendProcess($proc.Handle) }";

        try {
            // Encode the script to Base64 to bypass any complex quoting issues in the Java
            // Runtime exec
            String encodedScript = java.util.Base64.getEncoder()
                    .encodeToString(script.getBytes(java.nio.charset.StandardCharsets.UTF_16LE));
            Runtime.getRuntime()
                    .exec(new String[] { "powershell", "-WindowStyle", "Hidden", "-EncodedCommand", encodedScript });
            System.out.println("FirewallController: Suspended Process PID " + pid);
        } catch (IOException e) {
            System.err.println("FirewallController failed to suspend process: " + e.getMessage());
        }
    }
}
