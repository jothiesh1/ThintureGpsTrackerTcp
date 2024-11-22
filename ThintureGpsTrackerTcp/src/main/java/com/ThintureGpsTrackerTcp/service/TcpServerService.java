package com.ThintureGpsTrackerTcp.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.util.PacketParser;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
/*
 * 
 * 
 * 
 * 
 * imie 861435074725   629   
 * 1001110101
    ,A21 new device 
 *      861435074725   223
 *  
 * 
 * 
 * 
 * 
 * 
 */

import org.springframework.context.event.EventListener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
@Service
public class TcpServerService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TcpServerService.class);

    @Autowired
    private PacketParser packetParser; // Parses GPS data packets and handles broadcasting

    @Value("${tcp.server.port:5000}")
    private int tcpPort; // Configurable TCP port for the server (default: 5000)

    /**
     * Starts the TCP server automatically when the application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void startServer() {
        new Thread(this).start(); // Runs the server in a separate thread
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            logger.info("TCP Server started on port {}", tcpPort);

            // Continuously listen for client connections
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept(); // Accepts a new client
                    logger.info("New client connected from {}", clientSocket.getInetAddress());
                    new Thread(() -> handleClientConnection(clientSocket)).start(); // Handle each client in a separate thread
                } catch (Exception e) {
                    logger.error("Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start TCP server", e);
        }
    }

    /**
     * Handles an individual client connection.
     *
     * @param clientSocket The socket connected to the client.
     */
    private void handleClientConnection(Socket clientSocket) {
        try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
            byte[] buffer = new byte[1024]; // Buffer to store incoming data
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                // Convert the raw packet data to a readable string
                String packetString = new String(buffer, 0, bytesRead);

                // Extract the device ID (IMEI)
                String deviceID = extractDeviceID(packetString);
                if (deviceID == null) {
                    logger.warn("Unable to parse device ID from packet: {}", packetString);
                    continue;
                }

                // Log the formatted packet for debugging
                logFormattedPacket(deviceID, buffer, bytesRead);

                // Ensure the packet starts with the expected "$$" header
                if (buffer[0] == '$' && buffer[1] == '$') {
                    processPacket(deviceID, buffer, bytesRead); // Additional processing

                    // Extract and log the timestamp
                    String timestampHex = packetParser.extractHex(buffer, 0x04); // Assumes 0x04 is the timestamp field
                    Long timestampSeconds = null;
                    if (timestampHex != null) {
                        try {
                            byte[] hexData = packetParser.parseHexStringToByteArray(timestampHex);
                            timestampSeconds = packetParser.convertHexToTimestamp(hexData);
                            String formattedTimestamp = packetParser.formatTimestamp(timestampSeconds);
                            logger.info("\u001B[32mDevice {} - Parsed and formatted Timestamp: {}\u001B[0m", deviceID, formattedTimestamp);

                        } catch (IllegalArgumentException e) {
                            logger.error("Device {} - Timestamp parsing error: {}", deviceID, e.getMessage());
                        }
                    } else {
                        logger.warn("Device {} - Timestamp not found in packet.", deviceID);
                    }

                 // Extract latitude and longitude
                    byte[] latitudeBytes = extractBytes(buffer, 0x02, 0x03); // Assumes lat starts at 0x02
                    byte[] longitudeBytes = extractLongitudeAfterLatitude(buffer);

                    if (latitudeBytes != null && longitudeBytes != null) {
                        String latitudeHex = bytesToHex(latitudeBytes);
                        String longitudeHex = bytesToHex(longitudeBytes);

logger.info("\u001B[34mDevice {} - Extracted Latitude Hex: {}\u001B[0m", deviceID, latitudeHex);
logger.info("\u001B[34mDevice {} - Extracted Longitude Hex: {}\u001B[0m", deviceID, longitudeHex);

                        // Parse and broadcast the data
                        packetParser.extractAndBroadcastParameters(deviceID, latitudeHex, longitudeHex, buffer);
                    } else {
                        if (latitudeBytes == null) {
                            logger.warn("Device {} - Latitude section not found.", deviceID);
                        }
                        if (longitudeBytes == null) {
                            logger.warn("Device {} - Longitude section not found.", deviceID);
                        }
                    }


                    // Extract and log speed data
                    List<Integer> speeds = packetParser.extractAndLogSpeeds(buffer, deviceID);
                    if (!speeds.isEmpty()) {
                    //	logger.info("\u001B[36mDevice {} - Processed Speed(s): {}\u001B[0m", deviceID, speeds);

                    } else {
                        logger.warn("Device {} - No valid speed data found in the packet.", deviceID);
                    }

                    // Extract and decode the System Flag (0x1C)
                    Map<String, Boolean> systemFlags = packetParser.extractAndLogSystemFlags(buffer, deviceID);

                    // Check ignition status
                    boolean isIgnitionOn = systemFlags.getOrDefault("ACC Status (ON/OFF)", false);
                    logger.info("\u001B[1;36mDevice {}\u001B[0m - Ignition (ACC) Status: {}.",
                            deviceID,
                            isIgnitionOn ? "\u001B[1;32mON\u001B[0m" : "\u001B[1;31mOFF\u001B[0m");

                } else {
                    logger.warn("\u001B[33mDevice {} - Invalid GPS data format from client: {}\u001B[0m", deviceID, clientSocket.getInetAddress());
                }
            
             // Extract Mileage and Run Time
                Map<String, Object> extractedParams = packetParser.extractMileageAndRunTime(buffer, deviceID);

                // Extract Mileage
                Long mileageMeters = (Long) extractedParams.get("MileageMeters");
                Double mileageKilometers = (Double) extractedParams.get("MileageKilometers");

                // Log Mileage
                if (mileageMeters != null && mileageKilometers != null) {
                    logger.info("Device {} - Mileage: {} meters ({} kilometers)", deviceID, mileageMeters, mileageKilometers);
                } else {
                    logger.warn("Device {} - Mileage not found in the packet.", deviceID);
                }

                // Extract Run Time
                Long runTimeSeconds = (Long) extractedParams.get("RunTimeSeconds");
                String runTimeFormattedTime = (String) extractedParams.get("RunTimeFormattedTime");

                // Log Run Time
                if (runTimeSeconds != null && runTimeFormattedTime != null) {
                    logger.info("Device {} - Run Time: {} seconds", deviceID, runTimeSeconds);
                    logger.info("Device {} - Run Time formatted as time: {}", deviceID, runTimeFormattedTime);
                } else {
                    logger.warn("Device {} - Run Time not found in the packet.", deviceID);
                }

                
                
             // Extract Altitude
                Double altitude = packetParser.extractAltitude(buffer, deviceID);

                if (altitude != null) {
                    logger.info("Device {} - Altitude: {} meters", deviceID, altitude);
                } else {
                    logger.warn("Device {} - Altitude data not found or validation failed.", deviceID);
                }
             // Extract AD1, AD4, AD5 parameters
           
                Map<String, String> analogParams = packetParser.extractAnalogParameters(buffer, deviceID);

                String ad1 = analogParams.get("AD1");
                String ad4 = analogParams.get("AD4");
                String ad5 = analogParams.get("AD5");

                if (ad1 != null) {
                    logger.info("\u001B[34mDevice {} - AD1 Voltage: {}\u001B[0m", deviceID, ad1);
                } else {
                    logger.warn("Device {} - AD1 not found in the packet.", deviceID);
                }

                if (ad4 != null) {
                    logger.info("\u001B[32mDevice {} - AD4 Voltage and Battery: {}\u001B[0m", deviceID, ad4);
                } else {
                    logger.warn("Device {} - AD4 not found in the packet.", deviceID);
                }

                if (ad5 != null) {
                    logger.info("\u001B[36mDevice {} - AD5 Voltage: {}\u001B[0m", deviceID, ad5);
                } else {
                    logger.warn("Device {} - AD5 not found in the packet.", deviceID);
                }
                
                

             // Extract Driving Direction and HDOP parameters
                Map<String, Object> drivingParams = packetParser.extractDrivingDirectionAndHDOP(buffer, deviceID);

                Integer drivingDirection = (Integer) drivingParams.get("DrivingDirection");
                Double hdop = (Double) drivingParams.get("HDOP");

                if (drivingDirection != null) {
                    logger.info("\u001B[35mDevice {} - Driving Direction: {} degrees\u001B[0m", deviceID, drivingDirection);
                } else {
                    logger.warn("Device {} - Driving Direction not found in the packet.", deviceID);
                }

                if (hdop != null) {
                    logger.info("\u001B[33mDevice {} - HDOP: {:.1f}\u001B[0m", deviceID, hdop);
                } else {
                    logger.warn("Device {} - HDOP not found in the packet.", deviceID);
                }

                
             // Extract Additional Parameters
                Map<String, Object> gpsParams = packetParser.extractAdditionalParameters(buffer, deviceID);

                // Log GPS Positioning Status
                String gpsPositioningStatus = (String) gpsParams.get("GPSPositioningStatus");
                if (gpsPositioningStatus != null) {
                    logger.info("\u001B[34mDevice {} - GPS Positioning Status: {}\u001B[0m", deviceID, gpsPositioningStatus);
                } else {
                    logger.warn("Device {} - GPS Positioning Status not found in the packet.", deviceID);
                }

                // Log Number of Satellites
                Integer numberOfSatellites = (Integer) gpsParams.get("NumberOfSatellites");
                if (numberOfSatellites != null) {
                    logger.info("\u001B[36mDevice {} - Number of Satellites: {}\u001B[0m", deviceID, numberOfSatellites);
                } else {
                    logger.warn("Device {} - Number of Satellites not found in the packet.", deviceID);
                }

                // Log GSM Signal Strength
                Integer gsmSignalStrength = (Integer) gpsParams.get("GSMSignalStrength");
                if (gsmSignalStrength != null) {
                    logger.info("\u001B[32mDevice {} - GSM Signal Strength: {}\u001B[0m", deviceID, gsmSignalStrength);
                } else {
                    logger.warn("Device {} - GSM Signal Strength not found in the packet.", deviceID);
                }

                // Log Output Port Status
                String outputPortStatus = (String) gpsParams.get("OutputPortStatus");
                if (outputPortStatus != null) {
                    logger.info("\u001B[33mDevice {} - Output Port Status: {}\u001B[0m", deviceID, outputPortStatus);
                } else {
                    logger.warn("Device {} - Output Port Status not found in the packet.", deviceID);
                }

                // Log Input Port Status
                String inputPortStatus = (String) gpsParams.get("InputPortStatus");
                if (inputPortStatus != null) {
                    logger.info("\u001B[35mDevice {} - Input Port Status: {}\u001B[0m", deviceID, inputPortStatus);
                } else {
                    logger.warn("Device {} - Input Port Status not found in the packet.", deviceID);
                }

                // Log Geo-Fence Number
                Integer geoFenceNumber = (Integer) gpsParams.get("GeoFenceNumber");
                if (geoFenceNumber != null) {
                    logger.info("\u001B[31mDevice {} - Geo-Fence Number: {}\u001B[0m", deviceID, geoFenceNumber);
                } else {
                    logger.warn("Device {} - Geo-Fence Number not found in the packet.", deviceID);
                }

                
                
            }
        } catch (IOException e) {
            logger.error("Error handling client connection for client: {}", clientSocket.getInetAddress(), e);
        } finally {
            try {
                clientSocket.close();
                logger.info("Connection closed for client: {}", clientSocket.getInetAddress());
            } catch (IOException e) {
                logger.error("Error closing client socket: {}", clientSocket.getInetAddress(), e);
            }
        }
    }

 // Method to extract speeds from the buffer (placeholder, customize as needed)
    private List<Integer> extractSpeeds(byte[] buffer, String deviceID) {
        // TODO: Implement speed extraction logic
        return null;
    }

    // Logs formatted packet data in a readable hex format
    private void logFormattedPacket(String deviceID, byte[] buffer, int bytesRead) {
        StringBuilder hexOutput = new StringBuilder();
        hexOutput.append("[$$").append(deviceID).append(",");

        for (int i = 0; i < bytesRead; i++) {
            hexOutput.append(String.format("0x%02X", buffer[i]));
            if (i % 5 == 4) hexOutput.append(" "); // Add a space after every 5 bytes
            else if (i % 3 == 2) hexOutput.append("_"); // Add an underscore after every 3 bytes
        }
        hexOutput.append("]");
        logger.info("Device {} - Formatted packet data: {}", deviceID, hexOutput.toString().trim());
    }

    // Placeholder for actual packet processing logic (to be customized)
    private void processPacket(String deviceID, byte[] buffer, int bytesRead) {
        logger.debug("Processing packet for device {} with {} bytes read", deviceID, bytesRead);
        // TODO: Implement detailed packet processing logic here
    }

    // Extracts a hex string based on a marker (parameterId) from the buffer
    private String extractHex(byte[] buffer, int parameterId) {
        int parameterStartIndex = findMarkerIndex(buffer, 0, parameterId);
        if (parameterStartIndex == -1 || parameterStartIndex + 4 > buffer.length) {
            return null; // Marker not found or insufficient data
        }
        return bytesToHex(Arrays.copyOfRange(buffer, parameterStartIndex, parameterStartIndex + 4));
    }

    // Extracts the device ID (IMEI) from the packet string
    private String extractDeviceID(String packetString) {
        int startPos = packetString.indexOf("$$") + 2;
        int secondCommaIndex = packetString.indexOf(',', startPos);
        int thirdCommaIndex = packetString.indexOf(',', secondCommaIndex + 1);

        if (secondCommaIndex == -1 || thirdCommaIndex == -1) {
            logger.warn("Device ID format error in packet: {}", packetString);
            return null; // Malformed packet string
        }

        String fullDeviceID = packetString.substring(startPos, thirdCommaIndex).trim();
        return fullDeviceID.replaceAll("^[^,]*,", "").trim(); // Cleans up unwanted characters
    }

    // Finds the index of a specific marker in the byte array
    private int findMarkerIndex(byte[] data, int startIndex, int marker) {
        for (int i = startIndex; i < data.length; i++) {
            if (data[i] == (byte) marker) {
                return i;
            }
        }
        return -1; // Marker not found
    }

    // Extracts bytes between a startMarker and an endMarker
    private byte[] extractBytes(byte[] data, int startMarker, int endMarker) {
        int start = -1;
        int end = -1;

        for (int i = 0; i < data.length; i++) {
            if (data[i] == (byte) startMarker && start == -1) {
                start = i + 1; // Start after the marker
            } else if (data[i] == (byte) endMarker && start != -1) {
                end = i;
                break; // End marker found
            }
        }

        if (start != -1 && end != -1) {
            byte[] result = new byte[end - start];
            System.arraycopy(data, start, result, 0, end - start);
            return result;
        }
        return null; // Range not found
    }

    // Extracts longitude bytes immediately after latitude in the byte array
    private byte[] extractLongitudeAfterLatitude(byte[] data) {
        int latitudeStartIndex = findMarkerIndex(data, 0, 0x02);
        if (latitudeStartIndex == -1) return null;

        int latitudeEndIndex = findMarkerIndex(data, latitudeStartIndex + 1, 0x03);
        if (latitudeEndIndex == -1) return null;

        int longitudeStartIndex = latitudeEndIndex + 1;
        if (longitudeStartIndex + 4 >= data.length) return null;

        return Arrays.copyOfRange(data, longitudeStartIndex, longitudeStartIndex + 4);
    }

    // Converts a byte array into a space-separated hex string
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexBuilder.append(String.format("0x%02X ", b));
        }
        return hexBuilder.toString().trim();
    }

    // Parses a space-separated hex string into a byte array
    private static byte[] parseHexStringToByteArray(String hexString) {
        String[] hexValues = hexString.trim().split("\\s+");
        byte[] byteArray = new byte[hexValues.length];

        for (int i = 0; i < hexValues.length; i++) {
            String hex = hexValues[i].startsWith("0x") ? hexValues[i].substring(2) : hexValues[i];
            byteArray[i] = (byte) Integer.parseInt(hex, 16);
        }

        return byteArray;
    }
}