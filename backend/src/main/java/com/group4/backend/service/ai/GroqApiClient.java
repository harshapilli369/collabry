package com.group4.backend.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GroqApiClient {

    /** Low temperature for structured JSON responses (deterministic). */
    private static final double TEMPERATURE_JSON = 0.1;
    /** Higher temperature for creative text completions. */
    private static final double TEMPERATURE_TEXT = 0.7;

    @Value("${spring.ai.groq.api-key:dummy}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /** Default for Spring: real HTTP client. */
    public GroqApiClient() {
        this(new RestTemplate(), new ObjectMapper());
    }

    /**
     * Package-private for tests: inject a mock {@link RestTemplate} to avoid network calls.
     */
    public GroqApiClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return apiKey != null && !"dummy".equals(apiKey) && !apiKey.isBlank();
    }

    public String getChatCompletion(String prompt) {

        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(message));
        requestBody.put("response_format", Map.of("type", "json_object"));
        requestBody.put("temperature", TEMPERATURE_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String responseStr = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(responseStr);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"recommendations\": []}";
        }
    }

    public String getTextCompletion(String prompt) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", TEMPERATURE_TEXT);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String responseStr = restTemplate.postForObject(url, entity, String.class);
        try {
            JsonNode root = objectMapper.readTree(responseStr);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
}
