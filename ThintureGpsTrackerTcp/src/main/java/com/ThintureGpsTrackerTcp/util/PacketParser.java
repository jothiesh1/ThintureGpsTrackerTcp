package com.ThintureGpsTrackerTcp.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ThintureGpsTrackerTcp.handler.GPSWebSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PacketParser {

    private static final Logger logger = LoggerFactory.getLogger(PacketParser.class);
    private final GPSWebSocketHandler gpsWebSocketHandler;

    @Autowired
    public PacketParser(GPSWebSocketHandler gpsWebSocketHandler) {
        this.gpsWebSocketHandler = gpsWebSocketHandler;
    }

    /**
     * Extracts and broadcasts latitude and longitude coordinates for a device.
     * Converts hex values to decimal degrees and sends the coordinates via WebSocket.
     *
     * @param deviceID     Unique identifier for the device
     * @param latitudeHex  Hexadecimal string representing latitude
     * @param longitudeHex Hexadecimal string representing longitude
     */
    public void extractAndBroadcastParameters(String deviceID, String latitudeHex, String longitudeHex) {
        Double latitude = null;
        Double longitude = null;

        logger.debug("Starting extraction and broadcast of parameters for device {}", deviceID);

        try {
            latitude = convertHexToDegrees(latitudeHex);
            logger.info("Device {} - Parsed Latitude (Decimal Degrees): {} degrees", deviceID, latitude);
        } catch (IllegalArgumentException e) {
            logger.error("Device {} - Latitude conversion error: {}", deviceID, e.getMessage());
        }

        try {
            longitude = convertHexToDegrees(longitudeHex);
            logger.info("Device {} - Parsed Longitude (Decimal Degrees): {} degrees", deviceID, longitude);
        } catch (IllegalArgumentException e) {
            logger.error("Device {} - Longitude conversion error: {}", deviceID, e.getMessage());
        }

        // Broadcast latitude and longitude if both are valid
        if (latitude != null && longitude != null) {
            try {
                gpsWebSocketHandler.broadcastLocation(deviceID, latitude, longitude);
                logger.debug("Device {} - Broadcasted latitude and longitude successfully.", deviceID);
            } catch (Exception e) {
                logger.error("Device {} - Error broadcasting location via WebSocket", deviceID, e);
            }
        } else {
            logger.warn("Device {} - Invalid latitude or longitude, broadcasting skipped.", deviceID);
        }
    }

    /**
     * Converts a hexadecimal string to decimal degrees.
     *
     * @param hexData Hexadecimal string (e.g., "0xA9 0x7F 0xC7 0x00")
     * @return Decimal degree representation
     * @throws IllegalArgumentException if the hex data format is incorrect
     */
    public static double convertHexToDegrees(String hexData) {
        String[] hexValues = hexData.trim().split("\\s+");
        if (hexValues.length != 3 && hexValues.length != 4) {
            throw new IllegalArgumentException("Incorrect number of hex values for conversion.");
        }

        long decimalValue = 0;
        for (int i = hexValues.length - 1; i >= 0; i--) {
            decimalValue = (decimalValue << 8) | Integer.parseInt(hexValues[i].substring(2), 16);
        }

        // Handle 3-byte latitude data with sign extension for negative values
        if (hexValues.length == 3 && decimalValue >= 0x800000L) {
            decimalValue -= 0x1000000L;
        }

        double degrees = decimalValue / 1_000_000.0;
        logger.debug("Converted hex {} to degrees: {}", hexData, degrees);
        return degrees;
    }
}




/*
@Component
public class PacketParser {

    private static final Logger logger = LoggerFactory.getLogger(PacketParser.class);
    private final GPSWebSocketHandler gpsWebSocketHandler;

    @Autowired
    public PacketParser(GPSWebSocketHandler gpsWebSocketHandler) {
        this.gpsWebSocketHandler = gpsWebSocketHandler;
    }

    /**
     * Extracts and logs latitude and longitude hex values, converting them into decimal degrees.
     *
     * @param latitudeHex  The hex string for latitude (e.g., "0xA9 0x7F 0xC7 0x00")
     * @param longitudeHex The hex string for longitude (e.g., "0xF5 0x75 0x9F 0x04")
     
    public void extractAndBroadcastParameters(String latitudeHex, String longitudeHex) {
        Double latitude = null;
        Double longitude = null;

        if (latitudeHex != null) {
            try {
                latitude = convertHexToDegrees(latitudeHex);
                logger.info("Latitude Hex Values: {}", latitudeHex);
                logger.info("Parsed Latitude (Decimal Degrees): {} degrees", latitude);
            } catch (IllegalArgumentException e) {
                logger.error("Latitude conversion error: {}", e.getMessage());
            }
        } else {
            logger.warn("Latitude data could not be parsed or is incorrectly formatted.");
        }

        if (longitudeHex != null) {
            try {
                longitude = convertHexToDegrees(longitudeHex);
                logger.info("Longitude Hex Values: {}", longitudeHex);
                logger.info("Parsed Longitude (Decimal Degrees): {} degrees", longitude);
            } catch (IllegalArgumentException e) {
                logger.error("Longitude conversion error: {}", e.getMessage());
            }
        } else {
            logger.warn("Longitude data could not be parsed or is incorrectly formatted.");
        }

        if (latitude != null && longitude != null) {
            gpsWebSocketHandler.broadcastLocation(latitude, longitude);
        }
    }

    /**
     * Converts a hex string to decimal degrees. Supports both 3-byte (24-bit signed) and 4-byte (32-bit unsigned) hex values.
     *
     * @param hexData The hex string to convert (e.g., "0xA9 0x7F 0xC7 0x00")
     * @return The converted decimal degree value
     
    private double convertHexToDegrees(String hexData) {
        String[] hexValues = hexData.trim().split("\\s+");
        if (hexValues.length != 3 && hexValues.length != 4) {
            throw new IllegalArgumentException("Incorrect number of hex values for conversion. Expected 3 or 4 bytes.");
        }

        long decimalValue = 0;
        for (int i = hexValues.length - 1; i >= 0; i--) {
            decimalValue = (decimalValue << 8) | Integer.parseInt(hexValues[i].substring(2), 16);
        }

        if (hexValues.length == 3 && decimalValue >= 0x800000L) {
            decimalValue -= 0x1000000L;
        }

        double degrees = decimalValue / 1_000_000.0;
        logger.info("Hex [{}] converted to Degrees: {}", hexData, degrees);

        return degrees;
    }
}

*/
