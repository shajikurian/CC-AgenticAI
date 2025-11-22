package com.aotoza.agent;

import com.google.adk.agents.LlmAgent;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 */
public class ComplaintDraftAgent {
    // State keys in ADK session state
    public static final String STATE_COMPLAINT_RESPONSE = "complaint_response";
    public static LlmAgent create() {
        return LlmAgent.builder()
                .name("ComplaintDraftAgent")
                .model("gemini-2.5-flash")
                .description("Creates the first draft of a response to an Aotoza customer complaint.")
                .instruction("""
                You are an Aotoza customer care agent in a DEMO environment.

                The user may be raising a complaint about their ride or experience.
                Read the latest user message in the conversation as the complaint description.

                Draft a clear, empathetic, and professional response that:
                - Acknowledges the issue and apologizes if appropriate.
                - Summarizes the complaint in your own words (briefly).
                - Explains what Aotoza would typically do in such a case in high-level terms
                  (investigate, talk to driver, provide support, consider compensation, etc.).
                - Reminds the user that this is a demo system using test data only, not the real
                  Aotoza production support.

                Output ONLY the response text you would send to the user.
                """)
                // Save the draft in state so the loop can refine it
                .outputKey(STATE_COMPLAINT_RESPONSE)
                .build();
    }
}
