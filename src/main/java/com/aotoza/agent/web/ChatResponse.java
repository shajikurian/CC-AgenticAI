package com.aotoza.agent.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 11/20/2025
 * {@code @authors} shaji
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String sessionId;
    private String reply;
}
