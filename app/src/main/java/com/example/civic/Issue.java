package com.example.civic;

public class Issue {
    private String description;
    private String status;

    // NEW FIELDS
    private String userId;      // who reported the issue
    private double latitude;    // issue location
    private double longitude;   // issue location

    public Issue() {

    }

    public Issue(String description, String status) {
        this.description = description;
        this.status = status;
    }
    // Optional: another constructor when you also know user + location
    public Issue(String description, String status,
                 String userId, double latitude, double longitude) {
        this.description = description;
        this.status = status;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }
    // NEW GETTERS (no changes to old ones)
    public String getUserId() {
        return userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
