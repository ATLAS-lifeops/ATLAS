package com.example.atlas.setup.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class TelegramBotTokenValidator {

    private final RestClient.Builder restClientBuilder;

    public TelegramBotTokenValidator(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public TelegramBotIdentity validate(String botToken) {
        if (botToken == null || botToken.isBlank()) {
            throw new TelegramBotTokenValidationException("Telegram Bot Token is required.");
        }
        TelegramGetMeResponse response;
        try {
            response = restClientBuilder.clone()
                    .baseUrl("https://api.telegram.org/bot" + botToken.strip())
                    .build()
                    .get()
                    .uri("/getMe")
                    .retrieve()
                    .body(TelegramGetMeResponse.class);
        } catch (RestClientException exception) {
            throw new TelegramBotTokenValidationException("Telegram token could not be validated.");
        }

        if (response == null || !response.ok() || response.result() == null) {
            throw new TelegramBotTokenValidationException("Telegram token is invalid.");
        }

        return new TelegramBotIdentity(
                response.result().id(),
                response.result().username(),
                response.result().firstName()
        );
    }

    private record TelegramGetMeResponse(
            boolean ok,
            TelegramGetMeResult result
    ) {
    }

    private record TelegramGetMeResult(
            long id,
            String username,
            @JsonProperty("first_name") String firstName
    ) {
    }
}
