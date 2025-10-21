package com.project.smarthome.models;

import java.util.Date;
public class Home {
    private int id;
    private String name;
    private int user_id;

    public Home() {}

    public Home(int id, String name, int user_id) {
        this.id = id;
        this.name = name;
        this.user_id = user_id;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getUserId() { return user_id; }
    public void setUserId(int user_id) { this.user_id = user_id; }
}


