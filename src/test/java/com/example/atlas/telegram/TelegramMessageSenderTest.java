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

    @Test
    void replyMarkupIsAttachedToLastChunkOnly() {
        RecordingTelegramApiClient apiClient = new RecordingTelegramApiClient();
        TelegramMessageSender sender = new TelegramMessageSender(apiClient);
        InlineKeyboardMarkup keyboard = new TelegramKeyboardFactory().mainMenu();

        sender.sendText(42L, "a".repeat(TelegramMessageSender.MAX_TEXT_CHUNK_SIZE + 10), keyboard);

        assertThat(apiClient.sentMarkups()).hasSize(2);
        assertThat(apiClient.sentMarkups().get(0)).isNull();
        assertThat(apiClient.sentMarkups().get(1)).isSameAs(keyboard);
    }

    private static class RecordingTelegramApiClient implements TelegramApiClient {

        private final List<String> sentTexts = new ArrayList<>();
        private final List<InlineKeyboardMarkup> sentMarkups = new ArrayList<>();

        @Override
        public void sendMessage(long chatId, String text) {
            sentTexts.add(text);
            sentMarkups.add(null);
        }

        @Override
        public void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
            sentTexts.add(text);
            sentMarkups.add(replyMarkup);
        }

        List<String> sentTexts() {
            return sentTexts;
        }

        List<InlineKeyboardMarkup> sentMarkups() {
            return sentMarkups;
        }
    }
}
