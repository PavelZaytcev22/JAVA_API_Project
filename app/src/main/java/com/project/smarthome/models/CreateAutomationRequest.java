package com.project.smarthome.models;

class CreateAutomationRequest {
    private String name;
    private String trigger_type;
    private String trigger_value;
    private String action;
    private String schedule;

    public CreateAutomationRequest(String name, String trigger_type, String trigger_value,
                                   String action, String schedule) {
        this.name = name;
        this.trigger_type = trigger_type;
        this.trigger_value = trigger_value;
        this.action = action;
        this.schedule = schedule;
    }

    public String getName() {
        return name;
    }

    public String getTrigger_type() {
        return trigger_type;
    }

    public String getTrigger_value() {
        return trigger_value;
    }

    public String getAction() {
        return action;
    }

    public String getSchedule() {
        return schedule;
    }
}
