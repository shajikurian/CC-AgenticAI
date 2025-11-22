package com.aotoza.agent.web;

import lombok.Data;

/**
 * Created on 11/20/2025
 * {@code @authors} shaji
 */
@Data
public class ChatRequest {
    private String userId;     // e.g. "demo-user-1"
    private String sessionId;  // optional; if null, we create a new session
    private String message;
}