package com.project.smarthome.models.families;

public class FamilyCreateRequest {
    private String name;
    private int homeId;

    public FamilyCreateRequest(String name, int homeId) {
        this.name = name;
        this.homeId = homeId;
    }
}

