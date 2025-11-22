package com.aotoza.agent.config;

import com.aotoza.agent.GeneralSupportAgent;
import com.aotoza.agent.RideSupportAgent;
import com.aotoza.agent.RideTools;
import com.aotoza.agent.RootRouterAgent;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.tools.FunctionTool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 11/20/2025
 * {@code @authors} shaji
 */
@Configuration
public class AgentConfig {

    public static final String APP_NAME = "aotoza-support-app";

    /**
     * InMemoryRunner that wires the agent to a SessionService.
     * For capstone/demo, in-memory sessions are fine.
     */
    @Bean
    public InMemoryRunner inMemoryRunner(@Qualifier("rootRouterAgent") BaseAgent agent) {
        // If your ADK version has a two-arg ctor, use new InMemoryRunner(rootAgent, APP_NAME)
        return new InMemoryRunner(agent);
    }

    @Bean
    public BaseAgent rideAgent() {
        return RideSupportAgent.create();
    }

    @Bean
    public LlmAgent generalSupportAgent() {
        return GeneralSupportAgent.create();
    }

    @Bean
    public LlmAgent rootRouterAgent(@Qualifier("rideAgent") BaseAgent rideAgent,
                                    @Qualifier("generalSupportAgent") BaseAgent generalSupportAgent,
                                    @Qualifier("complaintWorkflowAgent") BaseAgent complaintWorkflowAgent) {
        return RootRouterAgent.create(rideAgent, generalSupportAgent, complaintWorkflowAgent);
    }
}