package com.ThintureGpsTrackerTcp.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        logger.info("New WebSocket connection established with session ID: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        logger.info("WebSocket connection closed with session ID: {}, Status: {}", session.getId(), status);
    }

    public void broadcastLocationWithIgnition(String deviceID, double latitude, double longitude, long timestamp, int speed, boolean isIgnitionOn, String ignitionState) {
        JSONObject locationData = new JSONObject();
        locationData.put("deviceID", deviceID);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("timestamp", timestamp);
        locationData.put("speed", speed);
        locationData.put("ignition", isIgnitionOn);
        locationData.put("ignitionState", ignitionState);

        TextMessage locationMessage = new TextMessage(locationData.toString());

        sessions.values().parallelStream().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(locationMessage);
                    logger.debug("Sent location with ignition statuses to session ID: {}", session.getId());
                } catch (IOException e) {
                    closeSessionGracefully(session);
                }
            } else {
                sessions.remove(session.getId());
            }
        });
    }

    private void closeSessionGracefully(WebSocketSession session) {
        try {
            session.close();
            sessions.remove(session.getId());
            logger.info("Closed WebSocket session ID gracefully: {}", session.getId());
        } catch (IOException e) {
            logger.error("Failed to close WebSocket session ID: {}", session.getId(), e);
        }
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
        logger.debug("Broadcasting location for device {}: {}", deviceID, locationData);

        // Iterate over the map entries safely
        sessions.entrySet().removeIf(entry -> {
            WebSocketSession session = entry.getValue();
            if (!session.isOpen()) {
                logger.warn("Session is closed. Removing session ID: {}", session.getId());
                return true; // Remove closed session
            }

            try {
                session.sendMessage(locationMessage);
                logger.info("Sent location to session ID: {}", session.getId());
                return false; // Keep session
            } catch (IOException e) {
                logger.error("Error sending WebSocket message to session ID: {}", session.getId(), e);
                return true; // Remove session on error
            }
        });
    }


}
