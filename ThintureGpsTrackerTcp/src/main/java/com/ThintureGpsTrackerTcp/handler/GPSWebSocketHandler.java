package com.ThintureGpsTrackerTcp.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashSet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
@Component
public class GPSWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GPSWebSocketHandler.class);
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("\u001B[32mNew WebSocket connection established with session ID: {}\u001B[0m", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("\u001B[31mWebSocket connection closed with session ID: {}, Status: {}\u001B[0m", session.getId(), status);
    }

    /**
     * Broadcasts location data with latitude, longitude, timestamp, and speed.
     *
     * @param deviceID  Unique identifier for the device
     * @param latitude  Latitude of the device
     * @param longitude Longitude of the device
     * @param timestamp Timestamp of the location data
     * @param speed     Speed of the device in km/h
     */
    public void broadcastLocationWithIgnition(String deviceID, double latitude, double longitude, long timestamp, int speed, boolean isIgnitionOn) {
        JSONObject locationData = new JSONObject();
        locationData.put("deviceID", deviceID);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("timestamp", timestamp);
        locationData.put("speed", speed);
        locationData.put("ignition", isIgnitionOn); // Add ignition status

        TextMessage locationMessage = new TextMessage(locationData.toString());
        logger.debug("\u001B[36mBroadcasting location with ignition status for device {}: {}\u001B[0m", deviceID, locationData);

        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(locationMessage);
                    logger.info("\u001B[32mSent location with ignition status to session ID: {}\u001B[0m", session.getId());
                } catch (IOException e) {
                    logger.error("\u001B[31mError sending WebSocket message to session ID: {}\u001B[0m", session.getId(), e);
                }
            } else {
                sessions.remove(session);
                logger.warn("\u001B[33mRemoved closed session ID: {}\u001B[0m", session.getId());
            }
        });
    }

    /**
     * Broadcasts only the basic location data.
     *
     * @param deviceID  Unique identifier for the device
     * @param latitude  Latitude of the device
     * @param longitude Longitude of the device
     */
    public void broadcastLocation(String deviceID, double latitude, double longitude) {
        JSONObject locationData = new JSONObject();
        locationData.put("deviceID", deviceID);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        TextMessage locationMessage = new TextMessage(locationData.toString());
        logger.debug("\u001B[34mBroadcasting location for device {}: {}\u001B[0m", deviceID, locationData);

        sessions.forEach(session -> {
            if (session.isOpen()) { // Only attempt to send if the session is open
                try {
                    session.sendMessage(locationMessage);
                    logger.info("\u001B[32mSent location to session ID: {}\u001B[0m", session.getId());
                } catch (IOException e) {
                    logger.error("\u001B[31mError sending WebSocket message to session ID: {}\u001B[0m", session.getId(), e);
                }
            } else {
                sessions.remove(session); // Remove closed session
                logger.warn("\u001B[33mRemoved closed session ID: {}\u001B[0m", session.getId());
            }
        });
    }
}
