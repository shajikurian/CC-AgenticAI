package com.aotoza.agent;

import com.google.adk.agents.LlmAgent;

import static com.aotoza.agent.ComplaintCriticAgent.STATE_COMPLAINT_CRITIQUE;
import static com.aotoza.agent.ComplaintDraftAgent.STATE_COMPLAINT_RESPONSE;
import static com.google.adk.agents.LlmAgent.IncludeContents.NONE;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 */
public class ComplaintRefinerAgent {
    public static LlmAgent create() {
        String instruction = String.format("""
            You are refining a complaint response for an Aotoza customer.

            Current response draft:
            \"\"\"
            {{%s}}
            \"\"\"

            Latest critique:
            {{%s}}

            Task:
            - Apply the critique to improve the response.
            - Keep the tone empathetic, polite, and professional.
            - Ensure the response:
              * Acknowledges the issue clearly.
              * Explains that this is a DEMO support system using test data.
              * Describes reasonable next steps in generic terms.

            Output ONLY the improved response text (this will replace the draft).
            """,
                STATE_COMPLAINT_RESPONSE,
                STATE_COMPLAINT_CRITIQUE
        );
        return LlmAgent.builder()
                .name("ComplaintRefinerAgent")
                .model("gemini-2.5-flash")
                .description("Refines the complaint response draft using the latest critique.")
                .instruction(instruction)
                .outputKey(STATE_COMPLAINT_RESPONSE) // overwrite draft with improved version
                .includeContents(NONE)
                .build();
    }
}
