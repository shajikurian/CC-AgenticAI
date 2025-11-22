package com.aotoza.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.web.AdkWebServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 */
public class DevUiRunner {

    public static void main(String[] args) {
        // Boot a minimal Spring context to get the rootRouterAgent bean:
        try (var ctx = new AnnotationConfigApplicationContext("com.aotoza.agent.config")) {
            BaseAgent root = ctx.getBean("rootRouterAgent", BaseAgent.class);
            AdkWebServer.start(root);
        }
    }
}
