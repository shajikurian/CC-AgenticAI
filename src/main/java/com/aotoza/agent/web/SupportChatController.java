package com.aotoza.agent.web;

import com.aotoza.agent.ConversationTraceStore;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Created on 11/20/2025
 * {@code @authors} shaji
 */
@Slf4j
@RestController
@RequestMapping("/api/support")
public class SupportChatController {
    @Autowired
    private ConversationTraceStore traceStore;

    private final InMemoryRunner runner;
    private final Counter requestCounter;
    private final Counter toolCallCounter;

    public SupportChatController(InMemoryRunner runner, MeterRegistry meterRegistry) {
        this.runner = runner;
        this.requestCounter = Counter.builder("aotoza.agent.requests.total")
                .description("Total number of chat requests to the Aotoza support agent")
                .register(meterRegistry);

        this.toolCallCounter = Counter.builder("aotoza.agent.tool_calls.total")
                .description("Total number of tool calls (e.g., get_user_ride_summary)")
                .register(meterRegistry);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        traceStore.add(request.getSessionId(), request.getUserId(), "user", request.getMessage());
        requestCounter.increment();
        String userId = (request.getUserId() == null || request.getUserId().isBlank())
                ? "demo-user"
                : request.getUserId();

        Session session;
        // Collect agent's content (can refine this later if we want streaming)
        StringBuilder responseText = new StringBuilder();

        try {
            if (request.getSessionId() == null || request.getSessionId().isBlank()) {
                // New conversation
                session = runner
                        .sessionService()
                        .createSession(runner.appName(), userId)
                        .blockingGet();
            } else {
                // Try to resume existing session.
                // NOTE: depending on ADK version, the getSession signature may differ slightly.
                session = runner
                        .sessionService()
                        .getSession(runner.appName(), userId, request.getSessionId(), Optional.empty())
                        .blockingGet();

                if (session == null) {
                    // Fallback: create new if not found
                    session = runner
                            .sessionService()
                            .createSession(runner.appName(), userId)
                            .blockingGet();
                }
            }
            MDC.put("sessionId", session.id());
            MDC.put("userId", session.userId());

            // Build user message content
            Content userMsg = Content.fromParts(Part.fromText(request.getMessage()));

            // Call the agent
            Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg);

            events.blockingForEach(event -> {
                if (!event.functionCalls().isEmpty()) {
                    toolCallCounter.increment();
                }

                log.info("ADK Event: {}", event);
                // Ignore events that are just tool calls / tool responses
                if (!event.functionCalls().isEmpty() || !event.functionResponses().isEmpty()) {
                    return;
                }

                if (event.content().isEmpty()) {
                    return;
                }

                Content content = event.content().get();

                if (content.parts().isEmpty()) {
                    return;
                }

                for (Part part : content.parts().get()) {
                    // We only care about plain text parts here
                    if (part.text().isPresent()) {
                        responseText.append(part.text().get());
                    }
                }
            });
        } catch (Exception ex) {
            // For now just fail hard; add better error handling / logging later.
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse(null, "Failed to create/resume session: " + ex.getMessage()));
        } finally {
            MDC.clear();
        }

        ChatResponse resp = new ChatResponse(session.id(), responseText.toString());
        traceStore.add(session.id(), userId, "assistant", responseText.toString());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/traces")
    public List<ConversationTraceStore.TraceEntry> traces() {
        return traceStore.all();
    }
}
