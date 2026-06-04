package com.example.atlas.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAICompatibleLlmClientTest {

    @Test
    void parsesSuccessfulResponse() {
        LlmResponse response = client().handleResponse(response(200, """
                {"id":"req-1","model":"test-model","choices":[{"message":{"content":"Plan text"},"finish_reason":"stop"}],"usage":{"prompt_tokens":4,"completion_tokens":5,"total_tokens":9}}
                """), "fallback-model");

        assertThat(response.text()).isEqualTo("Plan text");
        assertThat(response.model()).isEqualTo("test-model");
        assertThat(response.usage().totalTokens()).isEqualTo(9);
        assertThat(response.rawProviderRequestId()).isEqualTo("req-1");
    }

    @Test
    void mapsAuthErrorsToUnavailable() {
        assertThatThrownBy(() -> client().handleResponse(response(401, "{}"), "model"))
                .isInstanceOf(LlmUnavailableException.class)
                .hasMessageContaining("authentication");
    }

    @Test
    void mapsRateLimitToRateLimitException() {
        assertThatThrownBy(() -> client().handleResponse(response(429, "{}"), "model"))
                .isInstanceOf(LlmRateLimitException.class);
    }

    @Test
    void mapsTimeoutStatusToTimeoutException() {
        assertThatThrownBy(() -> client().handleResponse(response(504, "{}"), "model"))
                .isInstanceOf(LlmTimeoutException.class);
    }

    @Test
    void mapsProviderUnavailable() {
        assertThatThrownBy(() -> client().handleResponse(response(500, "{}"), "model"))
                .isInstanceOf(LlmUnavailableException.class);
    }

    @Test
    void mapsMalformedResponse() {
        assertThatThrownBy(() -> client().handleResponse(response(200, "{\"choices\":[]}"), "model"))
                .isInstanceOf(LlmClientException.class);
    }

    private OpenAICompatibleLlmClient client() {
        return new OpenAICompatibleLlmClient(
                "http://127.0.0.1:1",
                "test-key",
                Duration.ofSeconds(1),
                new ObjectMapper(),
                false,
                0
        );
    }

    private HttpResponse<String> response(int status, String body) {
        return new FakeHttpResponse(status, body);
    }

    private record FakeHttpResponse(int statusCode, String body) implements HttpResponse<String> {
        @Override
        public HttpRequest request() {
            return HttpRequest.newBuilder(URI.create("http://127.0.0.1")).build();
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(java.util.Map.of(), (left, right) -> true);
        }

        @Override
        public URI uri() {
            return URI.create("http://127.0.0.1");
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }

        @Override
        public Optional<javax.net.ssl.SSLSession> sslSession() {
            return Optional.empty();
        }
    }
}
