package com.toptha.app.model;

public class ProcessInfo {
    private int pid;
    private String name;
    private String executablePath;

    public ProcessInfo(int pid, String name, String executablePath) {
        this.pid = pid;
        this.name = name;
        this.executablePath = executablePath;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutablePath() {
        return executablePath;
    }

    public void setExecutablePath(String executablePath) {
        this.executablePath = executablePath;
    }

    @Override
    public String toString() {
        return name != null ? name : "Unknown";
    }
}
