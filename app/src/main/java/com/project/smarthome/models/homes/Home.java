package com.project.smarthome.models.homes;

public class Home {
    private int id;
    private String name;
    private int owner_id;

    public Home() {}

    public Home(int id, String name, int owner_id) {
        this.id = id;
        this.name = name;
        this.owner_id = owner_id;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getOwnerId() { return owner_id; }
    public void setOwnerId(int owner_id) { this.owner_id = owner_id; }
}