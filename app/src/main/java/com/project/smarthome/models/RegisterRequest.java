package com.project.smarthome.models;


public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String full_name;

    public RegisterRequest(String username, String password, String email, String full_name) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.full_name = full_name;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFullName() { return full_name; }
}

