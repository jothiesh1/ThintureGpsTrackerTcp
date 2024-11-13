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
        logger.info("New WebSocket connection established with session ID: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("WebSocket connection closed with session ID: {}, Status: {}", session.getId(), status);
    }

    public void broadcastLocation(String deviceID, double latitude, double longitude) {
        JSONObject locationData = new JSONObject();
        locationData.put("deviceID", deviceID);
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        TextMessage locationMessage = new TextMessage(locationData.toString());
        logger.debug("Broadcasting location for device {}: {}", deviceID, locationData);

        sessions.forEach(session -> {
            if (session.isOpen()) { // Only attempt to send if the session is open
                try {
                    session.sendMessage(locationMessage);
                    logger.debug("Sent location to session ID: {}", session.getId());
                } catch (IOException e) {
                    logger.error("Error sending WebSocket message to session ID: {}", session.getId(), e);
                }
            } else {
                sessions.remove(session); // Remove closed session
                logger.info("Removed closed session ID: {}", session.getId());
            }
        });
    }
}