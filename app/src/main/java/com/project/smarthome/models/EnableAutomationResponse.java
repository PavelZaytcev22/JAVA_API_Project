package com.project.smarthome.models;

class EnableAutomationResponse {
    private String status;
    private int automation;
    private boolean enabled;
    private String message;

    public String getStatus() {
        return status;
    }

    public int getAutomation() {
        return automation;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getMessage() {
        return message;
    }
}
