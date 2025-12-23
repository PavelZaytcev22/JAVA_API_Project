package com.project.smarthome.models;
class EnableAutomationRequest {
    private boolean enabled;

    public EnableAutomationRequest(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() { return enabled; }
}

