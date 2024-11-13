package com.ThintureGpsTrackerTcp.DTO;
public class DeviceLocation {
    private double latitude;
    private double longitude;

    public DeviceLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
//
//