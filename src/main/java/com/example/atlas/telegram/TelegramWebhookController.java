package com.example.atlas.telegram;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/telegram")
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramWebhookController {

    private final TelegramBotAdapter botAdapter;

    public TelegramWebhookController(TelegramBotAdapter botAdapter) {
        this.botAdapter = botAdapter;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveUpdate(@RequestBody(required = false) TelegramUpdate update) {
        botAdapter.handleUpdate(update);
        return ResponseEntity.ok().build();
    }
}
