package org.acme.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Singleton;

@Singleton
public class SessionManager {
    private final Map<String, String> SESSIONS = new ConcurrentHashMap<>();

    public void startSession(String userName, String connectionKey) {
        SESSIONS.put(userName.toLowerCase(), connectionKey);
    }

    public void endSession(String userName) {
        SESSIONS.remove(userName.toLowerCase());
    }

    public String getConnectionKey(String userName) {
        return SESSIONS.get(userName.toLowerCase());
    }

    public boolean isSessionExists(String userName) {
        return SESSIONS.containsKey(userName.toLowerCase());
    }
}
