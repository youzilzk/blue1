package com.youzi.blue.net.client.entity;



import java.io.Serializable;



public class User implements Serializable {

    private String id;

    private String username;

    private String password;

    private String icon;

    private String description;

    private String macAddress;

    public User() {
    }

    public User(String id, String username, String password, String icon, String description, String macAddress) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.icon = icon;
        this.description = description;
        this.macAddress = macAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
