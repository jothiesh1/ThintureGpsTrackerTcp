package com.ThintureGpsTrackerTcp.model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "device_history")
public class DeviceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Integer speed;
    private String ignitionState;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getSpeed() {
		return speed;
	}
	public void setSpeed(Integer speed) {
		this.speed = speed;
	}
	public String getIgnitionState() {
		return ignitionState;
	}
	public void setIgnitionState(String ignitionState) {
		this.ignitionState = ignitionState;
	}

	@Override
	public String toString() {
	    return "DeviceHistory{" +
	           "id=" + id +
	           ", deviceId='" + deviceId + '\'' +
	           ", latitude=" + latitude +
	           ", longitude=" + longitude +
	           ", timestamp=" + timestamp +
	           ", speed=" + speed +
	           ", ignitionState='" + ignitionState + '\'' +
	           '}';
	}

}
