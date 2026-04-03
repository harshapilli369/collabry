package com.group4.backend.service;
import com.group4.backend.service.ai.GroqApiClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroqApiClientTest {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Mock
    private RestTemplate restTemplate;

    private GroqApiClient groqApiClient;

    @BeforeEach
    void setUp() {
        groqApiClient = new GroqApiClient(restTemplate, new ObjectMapper());
    }

    @Test
    void isConfigured_returnsFalseWhenApiKeyIsDummy() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "dummy");

        assertThat(groqApiClient.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalseWhenApiKeyIsNull() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", null);

        assertThat(groqApiClient.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsFalseWhenApiKeyIsBlank() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "   ");

        assertThat(groqApiClient.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_returnsTrueWhenApiKeyIsSet() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "gsk-real-key");

        assertThat(groqApiClient.isConfigured()).isTrue();
    }

    @Test
    void getChatCompletion_returnsContentFromGroqJsonResponse() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "test-key");
        String groqBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"recommendations\\": []}"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.postForObject(eq(GROQ_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(groqBody);

        String result = groqApiClient.getChatCompletion("hello");

        assertThat(result).isEqualTo("{\"recommendations\": []}");
    }

    @Test
    void getChatCompletion_onHttpOrParseError_returnsEmptyRecommendationsJson() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "test-key");
        when(restTemplate.postForObject(eq(GROQ_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("network down"));

        String result = groqApiClient.getChatCompletion("hello");

        assertThat(result).isEqualTo("{\"recommendations\": []}");
    }

    @Test
    void getChatCompletion_onMalformedGroqJson_returnsEmptyRecommendationsJson() {
        ReflectionTestUtils.setField(groqApiClient, "apiKey", "test-key");
        when(restTemplate.postForObject(eq(GROQ_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn("not json");

        String result = groqApiClient.getChatCompletion("hello");

        assertThat(result).isEqualTo("{\"recommendations\": []}");
    }
}
