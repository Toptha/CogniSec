package com.toptha.app.model;

import java.time.LocalDateTime;

public class Alert {
    private long id;
    private LocalDateTime timestamp;
    private String type;
    private String message;
    private String actionTaken;

    public Alert(LocalDateTime timestamp, String type, String message, String actionTaken) {
        this.timestamp = timestamp;
        this.type = type;
        this.message = message;
        this.actionTaken = actionTaken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }
}
