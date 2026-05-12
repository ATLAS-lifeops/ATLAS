package com.example.atlas.telegram;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TelegramMessageSenderTest {

    @Test
    void shortResponseStaysAsOneMessage() {
        RecordingTelegramApiClient apiClient = new RecordingTelegramApiClient();
        TelegramMessageSender sender = new TelegramMessageSender(apiClient);

        sender.sendText(42L, "short reply");

        assertThat(apiClient.sentTexts()).containsExactly("short reply");
    }

    @Test
    void longResponseSplitsIntoChunksWithinLimit() {
        TelegramMessageSender sender = new TelegramMessageSender(new RecordingTelegramApiClient());
        String longText = "a".repeat(TelegramMessageSender.MAX_TEXT_CHUNK_SIZE)
                + "\n\n"
                + "b".repeat(200);

        List<String> chunks = sender.splitText(longText);

        assertThat(chunks).hasSize(2);
        assertThat(chunks).allSatisfy(chunk ->
                assertThat(chunk.length()).isLessThanOrEqualTo(TelegramMessageSender.MAX_TEXT_CHUNK_SIZE)
        );
        assertThat(String.join("\n\n", chunks)).contains("a".repeat(100), "b".repeat(100));
    }

    @Test
    void sendErrorsDoNotPropagate() {
        TelegramMessageSender sender = new TelegramMessageSender((chatId, text) -> {
            throw new IllegalStateException("telegram transport failed");
        });

        assertThatCode(() -> sender.sendText(42L, "reply"))
                .doesNotThrowAnyException();
    }

    private static class RecordingTelegramApiClient implements TelegramApiClient {

        private final List<String> sentTexts = new ArrayList<>();

        @Override
        public void sendMessage(long chatId, String text) {
            sentTexts.add(text);
        }

        List<String> sentTexts() {
            return sentTexts;
        }
    }
}
