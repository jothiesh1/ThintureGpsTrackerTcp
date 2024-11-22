package com.ThintureGpsTrackerTcp.DTO;
public class DeviceLocation {
    private double latitude;
    private double longitude;
    private Integer speed; // Speed in km/h
    private Long timestamp; // Timestamp in seconds since epoch

    // Constructor with latitude and longitude
    public DeviceLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Constructor with all fields
    public DeviceLocation(double latitude, double longitude, Integer speed, Long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.timestamp = timestamp;
    }

    // Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + (speed != null ? speed + " km/h" : "N/A") +
                ", timestamp=" + (timestamp != null ? timestamp : "N/A") +
                '}';
    }
}
