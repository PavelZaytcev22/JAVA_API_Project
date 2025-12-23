package com.project.smarthome.models.devices;

public class Device {

    // === SERVER FIELDS ===
    private int id;
    private String name;
    private String type;
    private Integer room_id;
    private int home_id;
    private String state;
    private String last_update; // ISO-8601

    private boolean pending;   // запрос отправлен
    private boolean deleted;

    public Device() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getRoomId() { return room_id; }
    public void setRoomId(Integer room_id) { this.room_id = room_id; }

    public int getHomeId() { return home_id; }
    public void setHomeId(int home_id) { this.home_id = home_id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLastUpdate() { return last_update; }
    public void setLastUpdate(String last_update) {
        this.last_update = last_update;
    }

    public boolean isPending() { return pending; }
    public void setPending(boolean pending) { this.pending = pending; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
