package com.aotoza.agent;

import com.google.adk.agents.LlmAgent;

import static com.aotoza.agent.ComplaintDraftAgent.STATE_COMPLAINT_RESPONSE;
import static com.google.adk.agents.LlmAgent.IncludeContents.NONE;

/**
 * Created on 11/21/2025
 * {@code @authors} shaji
 */
public class ComplaintCriticAgent {
    public static final String STATE_COMPLAINT_CRITIQUE = "complaint_critique";
    public static LlmAgent create() {
        String instruction = String.format("""
                You are reviewing a DRAFT response to an Aotoza customer complaint.
                
                Current response draft:
                \"\"
                {{%s}}
                \"\"
                
                Task:
                - Evaluate tone (empathy, politeness, professionalism).
                - Check clarity and structure.
                - Check whether it clearly explains next steps for the customer.
                - Check if it clearly states this is a DEMO, not production.
                
                If you see 1–3 clear improvements:
                - Provide concise, actionable critique (bullet points or short sentences).
                - Focus on concrete edits (e.g., "Add explicit apology", "Clarify next steps").
                
                If the draft is already good enough to send:
                - Reply with a short message like:
                  "Looks good to send; only minor or no changes needed."
                
                Output ONLY the critique text (no revised draft).
                """, STATE_COMPLAINT_RESPONSE);
        return LlmAgent.builder()
                .name("ComplaintCriticAgent")
                .model("gemini-2.5-flash")
                .description("Reviews the current complaint response and provides critique.")
                .instruction(instruction)
                .outputKey(STATE_COMPLAINT_CRITIQUE)
                .includeContents(NONE)
                .build();
    }
}
