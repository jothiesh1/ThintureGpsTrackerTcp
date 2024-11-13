package com.ThintureGpsTrackerTcp.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ThintureGpsTrackerTcp.util.PacketParser;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Arrays;

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
    private PacketParser packetParser;

    @Value("${tcp.server.port:5000}")
    private int tcpPort;

    @EventListener(ApplicationReadyEvent.class)
    public void startServer() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            logger.info("TCP Server started on port {}", tcpPort);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New client connected from {}", clientSocket.getInetAddress());
                    new Thread(() -> handleClientConnection(clientSocket)).start();
                } catch (Exception e) {
                    logger.error("Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to start TCP server", e);
        }
    }

    private void handleClientConnection(Socket clientSocket) {
        try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                String packetString = new String(buffer, 0, bytesRead);

                // Extract deviceID, latitude, and longitude
                String deviceID = parseField(packetString, 2, ",");
                String deviceType = parseField(packetString, deviceID.length() + 4, ",");
                String identifier = parseField(packetString, deviceType.length() + 4, ",");
                String status = parseField(packetString, deviceType.length() + identifier.length() + 5, ",");

                logFormattedPacket(deviceType, identifier, status, buffer, bytesRead);

                if (buffer[0] == '$' && buffer[1] == '$') {
                    processPacket(deviceType, identifier, status, buffer, bytesRead);

                    // Extract latitude and longitude
                    byte[] latitudeBytes = extractBytes(buffer, 0x02, 0x03);
                    byte[] longitudeBytes = extractLongitudeAfterLatitude(buffer);

                    if (latitudeBytes != null && longitudeBytes != null) {
                        String latitudeHex = bytesToHex(latitudeBytes);
                        String longitudeHex = bytesToHex(longitudeBytes);

                        logger.info("Device {} - Extracted Latitude Hex: {}", deviceID, latitudeHex);
                        logger.info("Device {} - Extracted Longitude Hex: {}", deviceID, longitudeHex);

                        // Process the extracted coordinates
                        packetParser.extractAndBroadcastParameters(deviceID, latitudeHex, longitudeHex);
                    } else {
                        if (latitudeBytes == null) {
                            logger.warn("Device {} - Latitude section not found.", deviceID);
                        }
                        if (longitudeBytes == null) {
                            logger.warn("Device {} - Longitude section not found.", deviceID);
                        }
                    }
                } else {
                    logger.warn("Device {} - Invalid GPS data format from client: {}", deviceID, clientSocket.getInetAddress());
                }
            }
            logger.info("Connection closed for client: {}", clientSocket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error handling client connection", e);
        }
    }

    private String parseField(String packetString, int startPos, String delimiter) {
        int endPos = packetString.indexOf(delimiter, startPos);
        return (endPos != -1) ? packetString.substring(startPos, endPos) : "";
    }

    private void logFormattedPacket(String deviceType, String identifier, String status, byte[] data, int length) {
        StringBuilder hexOutput = new StringBuilder();
        hexOutput.append("[$$").append(deviceType).append(",")
                 .append(identifier).append(",")
                 .append(status).append(",");

        for (int i = 0; i < length; i++) {
            hexOutput.append(String.format("0x%02X", data[i]));
            if (i % 5 == 4) hexOutput.append(" ");
            else if (i % 3 == 2) hexOutput.append("_");
        }
        hexOutput.append("]");
        logger.info("Formatted packet data: {}", hexOutput.toString().trim());
    }

    private void processPacket(String deviceType, String identifier, String status, byte[] data, int length) {
        try {
            String hexData = toHexString(data, 0, length);
            logger.info("Hex Data: {}", hexData);
            logParsedFields(hexData);
        } catch (Exception e) {
            logger.error("Error processing packet", e);
        }
    }

    private void logParsedFields(String hexData) {
        String headerType = hexData.substring(0, 4);
        String dataIdentifier = hexData.substring(4, 6);
        String commandType = hexData.substring(8, 12);

        logger.info("Header Type: {}", headerType);
        logger.info("Data Identifier: {}", dataIdentifier);
        logger.info("Command Type: {}", commandType);
    }

    private String toHexString(byte[] data, int start, int length) {
        StringBuilder hexOutput = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            hexOutput.append(String.format("%02X", data[i]));
        }
        return hexOutput.toString();
    }

    private byte[] extractBytes(byte[] data, int startMarker, int endMarker) {
        int start = -1;
        int end = -1;

        for (int i = 0; i < data.length; i++) {
            if (data[i] == (byte) startMarker && start == -1) {
                start = i + 1;
            } else if (data[i] == (byte) endMarker && start != -1) {
                end = i;
                break;
            }
        }

        if (start != -1 && end != -1) {
            byte[] result = new byte[end - start];
            System.arraycopy(data, start, result, 0, end - start);
            return result;
        }
        return null;
    }

    private byte[] extractLongitudeAfterLatitude(byte[] data) {
        int latitudeStartIndex = findMarkerIndex(data, 0, 0x02);
        if (latitudeStartIndex == -1) return null;

        int latitudeEndIndex = findMarkerIndex(data, latitudeStartIndex + 1, 0x03);
        if (latitudeEndIndex == -1) return null;

        int longitudeStartIndex = latitudeEndIndex + 1;
        if (longitudeStartIndex + 4 >= data.length) return null;

        return Arrays.copyOfRange(data, longitudeStartIndex, longitudeStartIndex + 4);
    }

    private int findMarkerIndex(byte[] data, int startIndex, int marker) {
        for (int i = startIndex; i < data.length; i++) {
            if (data[i] == (byte) marker) {
                return i;
            }
        }
        return -1;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexBuilder.append(String.format("0x%02X ", b));
        }
        return hexBuilder.toString().trim();
    }
}
