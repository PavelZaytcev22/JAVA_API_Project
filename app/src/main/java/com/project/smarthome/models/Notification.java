package com.project.smarthome.models;

import java.util.Date;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String type;
    private Date created_at;
    private boolean is_read;

    public Notification() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getCreatedAt() { return created_at; }
    public void setCreatedAt(Date created_at) { this.created_at = created_at; }

    public boolean isRead() { return is_read; }
    public void setRead(boolean read) { is_read = read; }
}
