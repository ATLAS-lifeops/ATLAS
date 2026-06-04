package com.example.atlas.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

public class OpenAICompatibleLlmClient implements LlmClient {

    private final URI completionsUri;
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean retryEnabled;
    private final int maxRetries;

    public OpenAICompatibleLlmClient(
            String baseUrl,
            String apiKey,
            Duration connectTimeout,
            ObjectMapper objectMapper,
            boolean retryEnabled,
            int maxRetries
    ) {
        this.completionsUri = URI.create(stripTrailingSlash(baseUrl) + "/chat/completions");
        this.apiKey = apiKey == null ? "" : apiKey;
        this.objectMapper = objectMapper;
        this.retryEnabled = retryEnabled;
        this.maxRetries = Math.max(0, maxRetries);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout)
                .build();
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        String body = requestBody(request);
        int attempts = retryEnabled ? maxRetries + 1 : 1;
        LlmClientException lastFailure = null;

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder(completionsUri)
                        .timeout(request.timeout())
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (isRetryableStatus(response.statusCode()) && attempt < attempts) {
                    continue;
                }
                return handleResponse(response, request.model());
            } catch (java.net.http.HttpTimeoutException exception) {
                lastFailure = new LlmTimeoutException("LLM provider request timed out.", exception);
            } catch (IOException exception) {
                lastFailure = new LlmUnavailableException("LLM provider is unavailable.", exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new LlmUnavailableException("LLM provider request was interrupted.", exception);
            }

            if (attempt >= attempts || lastFailure instanceof LlmTimeoutException) {
                throw lastFailure;
            }
        }

        throw lastFailure == null ? new LlmUnavailableException("LLM provider is unavailable.") : lastFailure;
    }

    @Override
    public boolean available() {
        return true;
    }

    @Override
    public LlmProvider provider() {
        return LlmProvider.OPENAI_COMPATIBLE;
    }

    private String requestBody(LlmRequest request) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", request.model());
            ArrayNode messages = root.putArray("messages");
            for (LlmMessage message : request.messages()) {
                ObjectNode value = messages.addObject();
                value.put("role", message.role().name().toLowerCase(Locale.ROOT));
                value.put("content", message.content());
            }
            root.put("temperature", request.temperature());
            root.put("max_tokens", request.maxOutputTokens());
            return objectMapper.writeValueAsString(root);
        } catch (IOException exception) {
            throw new LlmClientException("Could not prepare LLM request.", exception);
        }
    }

    LlmResponse handleResponse(HttpResponse<String> response, String fallbackModel) {
        int status = response.statusCode();
        if (status == 401 || status == 403) {
            throw new LlmUnavailableException("LLM provider authentication or configuration failed.");
        }
        if (status == 408 || status == 504) {
            throw new LlmTimeoutException("LLM provider request timed out.");
        }
        if (status == 429) {
            throw new LlmRateLimitException("LLM provider rate limit reached.");
        }
        if (status >= 500) {
            throw new LlmUnavailableException("LLM provider is unavailable.");
        }
        if (status < 200 || status >= 300) {
            throw new LlmClientException("LLM provider returned an unsupported response.");
        }

        try {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choice = root.path("choices").path(0);
            String text = choice.path("message").path("content").asText("");
            if (text.isBlank()) {
                throw new LlmClientException("LLM provider returned an empty response.");
            }
            JsonNode usage = root.path("usage");
            return new LlmResponse(
                    text.strip(),
                    root.path("model").asText(fallbackModel),
                    LlmProvider.OPENAI_COMPATIBLE,
                    new LlmUsage(
                            integerOrNull(usage, "prompt_tokens"),
                            integerOrNull(usage, "completion_tokens"),
                            integerOrNull(usage, "total_tokens")
                    ),
                    choice.path("finish_reason").asText(""),
                    root.path("id").asText("")
            );
        } catch (IOException | IllegalArgumentException exception) {
            throw new LlmClientException("LLM provider returned a malformed response.", exception);
        }
    }

    private boolean isRetryableStatus(int status) {
        return status == 408 || status == 504 || status >= 500;
    }

    private Integer integerOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isInt() ? value.asInt() : null;
    }

    private static String stripTrailingSlash(String value) {
        String stripped = value == null ? "" : value.strip();
        while (stripped.endsWith("/")) {
            stripped = stripped.substring(0, stripped.length() - 1);
        }
        return stripped;
    }
}
