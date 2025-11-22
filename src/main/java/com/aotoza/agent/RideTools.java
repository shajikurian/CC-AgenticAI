package com.aotoza.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.ToolContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created on 11/20/2025
 * {@code @authors} shaji
 */
public class RideTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AtomicReference<JsonNode> CACHE = new AtomicReference<>();

    private static JsonNode loadData() {
        JsonNode cached = CACHE.get();
        if (cached != null) {
            return cached;
        }
        try (InputStream is = RideTools.class.getResourceAsStream("/data/rides.json")) {
            if (is == null) {
                throw new IllegalStateException("rides.json not found in /data");
            }
            JsonNode root = MAPPER.readTree(is);
            CACHE.set(root);
            return root;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rides.json", e);
        }
    }

    /**
     * Tool: look up a user's latest ride & request info.
     */
    @Schema(
            name = "get_user_ride_summary",
            description = """
            Fetches the latest ride and ride-request info for a user,
            given phone, email, or name. Returns keys like:
            'match_status', 'user_name', 'phone', 'latest_ride_status',
            'latest_ride_pickup', 'latest_ride_drop', 'latest_request_status', etc.
            """
    )
    public static Map<String, Object> getUserRideSummary(
            @Schema(
                    name = "identifier",
                    description = "User identifier: phone number, email, or part of the full name."
            )
            String identifier,
            @Schema(
                    name = "toolContext",
                    description = "Context for session state and logging."
            )
            ToolContext toolContext
    ) {
        System.out.printf("TOOL_EXEC: get_user_ride_summary('%s')%n", identifier);

        Map<String, Object> result = new HashMap<>();
        if (identifier == null || identifier.isBlank()) {
            result.put("match_status", "ERROR");
            result.put("error", "Identifier is empty.");
            return result;
        }

        identifier = identifier.trim().toLowerCase(Locale.ROOT);

        JsonNode root = loadData();
        JsonNode users = root.get("users");
        if (users == null || !users.isArray()) {
            result.put("match_status", "ERROR");
            result.put("error", "No users array in data file.");
            return result;
        }

        JsonNode match = null;
        Iterator<JsonNode> it = users.elements();
        while (it.hasNext()) {
            JsonNode u = it.next();
            String phone = u.path("phone").asText("");
            String email = u.path("email").asText("");
            String name = u.path("name").asText("");

            if (phone.toLowerCase(Locale.ROOT).contains(identifier)
                || email.toLowerCase(Locale.ROOT).contains(identifier)
                || name.toLowerCase(Locale.ROOT).contains(identifier)) {
                match = u;
                break;
            }
        }

        if (match == null) {
            result.put("match_status", "NOT_FOUND");
            result.put("message",
                    "No matching user found in the demo dataset for that identifier.");
            return result;
        }

        // Store memory in session state so follow-up questions work.
        String phone = match.path("phone").asText("");
        toolContext.state().put("user:last_identifier", identifier);
        toolContext.state().put("user:last_phone", phone);

        result.put("match_status", "FOUND");
        result.put("user_id", match.path("user_id").asText(""));
        result.put("user_name", match.path("name").asText(""));
        result.put("phone", phone);
        result.put("email", match.path("email").asText(""));

        JsonNode ride = match.path("latest_ride");
        if (!ride.isMissingNode() && !ride.isNull()) {
            result.put("latest_ride_id", ride.path("ride_id").asText(""));
            result.put("latest_ride_status", ride.path("status").asText(""));
            result.put("latest_ride_pickup", ride.path("pickup").asText(""));
            result.put("latest_ride_drop", ride.path("drop").asText(""));
            result.put("latest_ride_time", ride.path("timestamp").asText(""));
            result.put("latest_ride_fare", ride.path("fare").asDouble(0.0));
            result.put("latest_ride_payment_method",
                    ride.path("payment_method").asText(""));
            result.put("latest_ride_driver_name",
                    ride.path("driver_name").asText(""));
            result.put("latest_ride_vehicle_no",
                    ride.path("vehicle_no").asText(""));
            result.put("latest_ride_cancellation_reason",
                    ride.path("cancellation_reason").asText(""));
        }

        JsonNode req = match.path("latest_request");
        if (!req.isMissingNode() && !req.isNull()) {
            result.put("latest_request_id", req.path("request_id").asText(""));
            result.put("latest_request_status", req.path("status").asText(""));
            result.put("latest_request_time", req.path("timestamp").asText(""));
            result.put("latest_request_cancellation_reason",
                    req.path("cancellation_reason").asText(""));
        } else {
            result.put("latest_request_status", "NONE");
        }

        return result;
    }
}
