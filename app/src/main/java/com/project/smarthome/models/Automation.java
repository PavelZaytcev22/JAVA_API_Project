package com.project.smarthome.models;

public class Automation {
    private int id;
    private String name;
    private String trigger_type;
    private String trigger_value;
    private String action;
    private String schedule;
    private boolean enabled;
    private String created_at;
    private String updated_at;

    public Automation() {}

    public Automation(String name, String trigger_type, String trigger_value,
                      String action, String schedule, boolean enabled) {
        this.name = name;
        this.trigger_type = trigger_type;
        this.trigger_value = trigger_value;
        this.action = action;
        this.schedule = schedule;
        this.enabled = enabled;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTrigger_type() { return trigger_type; }
    public void setTrigger_type(String trigger_type) { this.trigger_type = trigger_type; }

    public String getTrigger_value() { return trigger_value; }
    public void setTrigger_value(String trigger_value) { this.trigger_value = trigger_value; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }
}

