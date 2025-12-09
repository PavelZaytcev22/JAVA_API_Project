package com.project.smarthome.models.homes;

public class Room {
    private int id;
    private String name;
    private int home_id;

    public Room() {}

    public Room(String name) {
        this.name = name;
    }

    public Room(int id, String name, int home_id) {
        this.id = id;
        this.name = name;
        this.home_id = home_id;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getHomeId() { return home_id; }
    public void setHomeId(int home_id) { this.home_id = home_id; }
}