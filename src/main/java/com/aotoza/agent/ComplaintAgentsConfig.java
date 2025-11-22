package com.aotoza.agent;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.LoopAgent;
import com.google.adk.agents.SequentialAgent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 * Complaint workflow:
 * - Draft a complaint response
 * - Iteratively refine it using a LoopAgent (critic + refiner)
 */
@Configuration
public class ComplaintAgentsConfig {

    /**
     * 1) Initial complaint responder – writes the first version of the reply.
     *    It reads the user's latest message from the conversation.
     */
    @Bean
    public LlmAgent complaintDraftAgent() {
        return ComplaintDraftAgent.create();
    }

    /**
     * 2a) Critic agent inside the loop – critiques the current draft.
     */
    @Bean
    public LlmAgent complaintCriticAgent() {
        return ComplaintCriticAgent.create();
    }

    /**
     * 2b) Refiner agent inside the loop – improves the draft based on critique.
     */
    @Bean
    public LlmAgent complaintRefinerAgent() {
        return ComplaintRefinerAgent.create();
    }

    /**
     * 2) LoopAgent that repeatedly runs Critic -> Refiner.
     *    Max 3 iterations to avoid infinite refinement.
     */
    @Bean
    public LoopAgent complaintRefinementLoop(LlmAgent complaintCriticAgent,
                                             LlmAgent complaintRefinerAgent) {
        return LoopAgent.builder()
                .name("ComplaintRefinementLoop")
                .description("Iteratively refines a complaint response draft up to 3 times.")
                .subAgents(complaintCriticAgent, complaintRefinerAgent)
                .maxIterations(3)
                .build();
    }

    /**
     * 3) Overall complaint workflow: Draft once, then refine in a loop.
     *    This will be plugged into the root router as a sub-agent.
     */
    @Bean
    public BaseAgent complaintWorkflowAgent(LlmAgent complaintDraftAgent,
                                            LoopAgent complaintRefinementLoop) {
        return SequentialAgent.builder()
                .name("complaint-workflow-agent")
                .description("Handles complaint responses using a draft + iterative refinement loop.")
                .subAgents(complaintDraftAgent, complaintRefinementLoop)
                .build();
    }
}
