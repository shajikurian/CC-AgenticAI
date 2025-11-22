package com.aotoza.agent;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 */
@Component
public class ConversationTraceStore {

    public static class TraceEntry {
        public final Instant timestamp;
        public final String sessionId;
        public final String userId;
        public final String role;     // "user" or "assistant"
        public final String message;

        public TraceEntry(Instant timestamp, String sessionId, String userId,
                          String role, String message) {
            this.timestamp = timestamp;
            this.sessionId = sessionId;
            this.userId = userId;
            this.role = role;
            this.message = message;
        }
    }

    private final List<TraceEntry> entries = new ArrayList<>();

    public synchronized void add(String sessionId, String userId,
                                 String role, String message) {
        entries.add(new TraceEntry(Instant.now(), sessionId, userId, role, message));
    }

    public synchronized List<TraceEntry> all() {
        return Collections.unmodifiableList(entries);
    }
}
