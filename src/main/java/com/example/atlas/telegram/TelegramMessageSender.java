package com.example.atlas.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "atlas.telegram", name = "enabled", havingValue = "true")
public class TelegramMessageSender {

    static final int MAX_TEXT_CHUNK_SIZE = 3900;

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageSender.class);

    private final TelegramApiClient telegramApiClient;

    public TelegramMessageSender(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public void sendText(long chatId, String text) {
        for (String chunk : splitText(text)) {
            try {
                telegramApiClient.sendMessage(chatId, chunk);
            } catch (RuntimeException exception) {
                log.warn(
                        "Telegram sendMessage failed for chat {} with {}",
                        chatId,
                        exception.getClass().getSimpleName()
                );
            }
        }
    }

    public List<String> splitText(String text) {
        String value = text == null || text.isBlank() ? TelegramReplyTemplates.generalFallback() : text;
        if (value.length() <= MAX_TEXT_CHUNK_SIZE) {
            return List.of(value);
        }

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < value.length()) {
            int end = Math.min(start + MAX_TEXT_CHUNK_SIZE, value.length());
            if (end == value.length()) {
                chunks.add(value.substring(start));
                break;
            }

            int splitAt = findSplitBoundary(value, start, end);
            chunks.add(value.substring(start, splitAt).stripTrailing());

            start = splitAt;
            while (start < value.length() && Character.isWhitespace(value.charAt(start))) {
                start++;
            }
        }

        return chunks;
    }

    private int findSplitBoundary(String text, int start, int end) {
        int paragraphBoundary = text.lastIndexOf("\n\n", end);
        if (paragraphBoundary > start) {
            return paragraphBoundary;
        }

        int lineBoundary = text.lastIndexOf('\n', end);
        if (lineBoundary > start) {
            return lineBoundary;
        }

        int wordBoundary = text.lastIndexOf(' ', end);
        if (wordBoundary > start) {
            return wordBoundary;
        }

        return end;
    }
}
