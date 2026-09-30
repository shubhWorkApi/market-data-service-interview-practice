package com.example.marketdata.service;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(String clientId, WebSocketSession newSession) {
        WebSocketSession oldSession = sessions.put(clientId, newSession);

        // Single-session rule: close the previous session when the same client connects again.
        if (oldSession != null && oldSession.isOpen() && !oldSession.getId().equals(newSession.getId())) {
            try {
                oldSession.close();
            } catch (Exception ignored) {
                // Cleanup should not prevent the new session from being registered.
            }
        }
    }

    public void remove(WebSocketSession session) {
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    public WebSocketSession get(String clientId) {
        return sessions.get(clientId);
    }
}
