package com.project.smarthome.models;


public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername(){return username;}

}
