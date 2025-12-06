package com.example.civic;

public class Report {

    public String reportId;
    public String description;
    public double latitude;
    public double longitude;
    public String photoUrl;
    public String videoUrl;
    public long timestamp;
    public String locationName;
    public String userId;

    public Report() {}

    public Report(String reportId, String description, double latitude, double longitude,
                  String photoUrl, String videoUrl, long timestamp, String locationName, String userId) {
        this.reportId = reportId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.photoUrl = photoUrl;
        this.videoUrl = videoUrl;
        this.timestamp = timestamp;
        this.locationName = locationName;
        this.userId = userId;
    }

    public String getReportId() { return reportId; }
    public String getDescription() { return description; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getPhotoUrl() { return photoUrl; }
    public String getVideoUrl() { return videoUrl; }
    public long getTimestamp() { return timestamp; }
    public String getLocationName() {
        return locationName;
    }
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public void setDescription(String description) { this.description = description; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
