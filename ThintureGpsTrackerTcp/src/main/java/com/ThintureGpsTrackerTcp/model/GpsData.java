package com.ThintureGpsTrackerTcp.model;

import java.util.Arrays;

public class GpsData {

    // Header Types
    private String headerType;
    private String dataIdentifier;

    // Core Data Information
    private int dataLength;
    private String imei;
    private String commandType;
    private int numberOfRemainingCacheRecords;
    private int numberOfDataPackets;
    private int lengthOfCurrentDataPacket;

    // GPS and Port Status
    private boolean gpsPositioningStatus; // true if valid, false if invalid
    private int numberOfSatellites;
    private int gsmSignalStrength;
    private byte outputPortStatus;
    private byte inputPortStatus;

    // Geo-fence and Miscellaneous
    private byte geoFenceNumber;
    private int systemFlag;
    private int iButtonId;

    // Battery and Voltage Parameters
    private int batteryLevel;
    private double ad1Voltage;
    private double ad4Voltage;
    private double ad5Voltage;

    // GPS Data Parameters
    private int speed;
    private int drivingDirection;
    private double hdop;
    private int altitude;
    private double latitude;
    private double longitude;

    // Time and Mileage
    private long dateTime;
    private int mileage;
    private int runTime;

    // Temperature and Humidity Sensors
    private double[] temperatureSensors = new double[8];

    // Network and Bluetooth Information
    private String networkInfo;
    private String bluetoothBeaconA;
    private String bluetoothBeaconB;

    // Additional Bluetooth and Sensor Data
    private String temperatureAndHumiditySensor;

    // Getters and Setters
    public String getHeaderType() {
        return headerType;
    }

    public void setHeaderType(String headerType) {
        this.headerType = headerType;
    }

    public String getDataIdentifier() {
        return dataIdentifier;
    }

    public void setDataIdentifier(String dataIdentifier) {
        this.dataIdentifier = dataIdentifier;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public int getNumberOfRemainingCacheRecords() {
        return numberOfRemainingCacheRecords;
    }

    public void setNumberOfRemainingCacheRecords(int numberOfRemainingCacheRecords) {
        this.numberOfRemainingCacheRecords = numberOfRemainingCacheRecords;
    }

    public int getNumberOfDataPackets() {
        return numberOfDataPackets;
    }

    public void setNumberOfDataPackets(int numberOfDataPackets) {
        this.numberOfDataPackets = numberOfDataPackets;
    }

    public int getLengthOfCurrentDataPacket() {
        return lengthOfCurrentDataPacket;
    }

    public void setLengthOfCurrentDataPacket(int lengthOfCurrentDataPacket) {
        this.lengthOfCurrentDataPacket = lengthOfCurrentDataPacket;
    }

    public boolean isGpsPositioningStatus() {
        return gpsPositioningStatus;
    }

    public void setGpsPositioningStatus(boolean gpsPositioningStatus) {
        this.gpsPositioningStatus = gpsPositioningStatus;
    }

    public int getNumberOfSatellites() {
        return numberOfSatellites;
    }

    public void setNumberOfSatellites(int numberOfSatellites) {
        this.numberOfSatellites = numberOfSatellites;
    }

    public int getGsmSignalStrength() {
        return gsmSignalStrength;
    }

    public void setGsmSignalStrength(int gsmSignalStrength) {
        this.gsmSignalStrength = gsmSignalStrength;
    }

    public byte getOutputPortStatus() {
        return outputPortStatus;
    }

    public void setOutputPortStatus(byte outputPortStatus) {
        this.outputPortStatus = outputPortStatus;
    }

    public byte getInputPortStatus() {
        return inputPortStatus;
    }

    public void setInputPortStatus(byte inputPortStatus) {
        this.inputPortStatus = inputPortStatus;
    }

    public byte getGeoFenceNumber() {
        return geoFenceNumber;
    }

    public void setGeoFenceNumber(byte geoFenceNumber) {
        this.geoFenceNumber = geoFenceNumber;
    }

    public int getSystemFlag() {
        return systemFlag;
    }

    public void setSystemFlag(int systemFlag) {
        this.systemFlag = systemFlag;
    }

    public int getiButtonId() {
        return iButtonId;
    }

    public void setiButtonId(int iButtonId) {
        this.iButtonId = iButtonId;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    

    public void setAd1Voltage(double d) {
        this.ad1Voltage = d;
    }

    public double getAd4Voltage() {
        return ad4Voltage;
    }

    public void setAd4Voltage(double ad4Voltage) {
        this.ad4Voltage = ad4Voltage;
    }

    public double getAd5Voltage() {
        return ad5Voltage;
    }

    public void setAd5Voltage(double ad5Voltage) {
        this.ad5Voltage = ad5Voltage;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDrivingDirection() {
        return drivingDirection;
    }

    public void setDrivingDirection(int drivingDirection) {
        this.drivingDirection = drivingDirection;
    }

    public double getHdop() {
        return hdop;
    }

    public void setHdop(double hdop) {
        this.hdop = hdop;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    public double[] getTemperatureSensors() {
        return temperatureSensors;
    }

    public void setTemperatureSensors(double[] temperatureSensors) {
        this.temperatureSensors = temperatureSensors;
    }

    public String getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(String networkInfo) {
        this.networkInfo = networkInfo;
    }

    public String getBluetoothBeaconA() {
        return bluetoothBeaconA;
    }

    public void setBluetoothBeaconA(String bluetoothBeaconA) {
        this.bluetoothBeaconA = bluetoothBeaconA;
    }

    public String getBluetoothBeaconB() {
        return bluetoothBeaconB;
    }

    public void setBluetoothBeaconB(String bluetoothBeaconB) {
        this.bluetoothBeaconB = bluetoothBeaconB;
    }

    public String getTemperatureAndHumiditySensor() {
        return temperatureAndHumiditySensor;
    }

    public void setTemperatureAndHumiditySensor(String temperatureAndHumiditySensor) {
        this.temperatureAndHumiditySensor = temperatureAndHumiditySensor;
    }

//toString method
@Override
public String toString() {
    return "GpsData{" +
            "headerType='" + headerType + '\'' +
            ", dataIdentifier='" + dataIdentifier + '\'' +
            ", dataLength=" + dataLength +
            ", imei='" + imei + '\'' +
            ", commandType='" + commandType + '\'' +
            ", numberOfRemainingCacheRecords=" + numberOfRemainingCacheRecords +
            ", numberOfDataPackets=" + numberOfDataPackets +
            ", lengthOfCurrentDataPacket=" + lengthOfCurrentDataPacket +
            ", gpsPositioningStatus=" + gpsPositioningStatus +
            ", numberOfSatellites=" + numberOfSatellites +
            ", gsmSignalStrength=" + gsmSignalStrength +
            ", outputPortStatus=" + outputPortStatus +
            ", inputPortStatus=" + inputPortStatus +
            ", geoFenceNumber=" + geoFenceNumber +
            ", systemFlag=" + systemFlag +
            ", iButtonId=" + iButtonId +
            ", batteryLevel=" + batteryLevel +
            ", ad1Voltage=" + ad1Voltage +
            ", ad4Voltage=" + ad4Voltage +
            ", ad5Voltage=" + ad5Voltage +
            ", speed=" + speed +
            ", drivingDirection=" + drivingDirection +
            ", hdop=" + hdop +
            ", altitude=" + altitude +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            ", dateTime=" + dateTime +
            ", mileage=" + mileage +
            ", runTime=" + runTime +
            ", temperatureSensors=" + Arrays.toString(temperatureSensors) +
            ", networkInfo='" + networkInfo + '\'' +
            ", bluetoothBeaconA='" + bluetoothBeaconA + '\'' +
            ", bluetoothBeaconB='" + bluetoothBeaconB + '\'' +
            ", temperatureAndHumiditySensor='" + temperatureAndHumiditySensor + '\'' +
            '}';
}
}