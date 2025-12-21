package com.project.smarthome.models.homes;

public class HomeCreateRequest {
    private String name;

    public HomeCreateRequest(String name) {
        this.name = name;
    }

    // Getter и Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}