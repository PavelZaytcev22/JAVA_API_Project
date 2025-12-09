package com.project.smarthome.models.families;

public class FamilyMemberAdd {
    private String username;

    public FamilyMemberAdd(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}