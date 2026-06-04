package com.example.atlas.telegram;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramMessageSender {

    static final int MAX_TEXT_CHUNK_SIZE = 3900;
    static final String ATLAS_PANEL_PHOTO = "https://raw.githubusercontent.com/ATLAS-lifeops/ATLAS/main/docs/assets/logo.png";

    private static final Logger log = LoggerFactory.getLogger(TelegramMessageSender.class);

    private final TelegramApiClient telegramApiClient;

    public TelegramMessageSender(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public void sendText(long chatId, String text) {
        sendText(chatId, text, null);
    }

    public void sendText(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        List<String> chunks = splitText(text);
        for (int index = 0; index < chunks.size(); index++) {
            String chunk = chunks.get(index);
            try {
                InlineKeyboardMarkup chunkMarkup = index == chunks.size() - 1 ? replyMarkup : null;
                telegramApiClient.sendMessage(chatId, chunk, chunkMarkup);
                log.info(
                        "Telegram sendMessage succeeded: chat_id={}, chunk_index={}, chunk_count={}, reply_markup_present={}",
                        chatId,
                        index + 1,
                        chunks.size(),
                        chunkMarkup != null
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "Telegram sendMessage failed: chat_id={}, chunk_index={}, chunk_count={}, reply_markup_present={}, error_type={}",
                        chatId,
                        index + 1,
                        chunks.size(),
                        index == chunks.size() - 1 && replyMarkup != null,
                        exception.getClass().getSimpleName()
                );
            }
        }
    }

    public void sendPanel(long chatId, String caption, InlineKeyboardMarkup replyMarkup) {
        try {
            telegramApiClient.sendPhoto(chatId, ATLAS_PANEL_PHOTO, caption, replyMarkup);
            log.info("Telegram sendPhoto panel succeeded: chat_id={}, reply_markup_present={}", chatId, replyMarkup != null);
        } catch (RuntimeException exception) {
            log.warn(
                    "Telegram sendPhoto panel failed: chat_id={}, reply_markup_present={}, error_type={}",
                    chatId,
                    replyMarkup != null,
                    exception.getClass().getSimpleName()
            );
            sendText(chatId, caption, replyMarkup);
        }
    }

    public void editPanel(long chatId, long messageId, String caption, InlineKeyboardMarkup replyMarkup) {
        try {
            telegramApiClient.editMessageCaption(chatId, messageId, caption, replyMarkup);
            log.info(
                    "Telegram editMessageCaption succeeded: chat_id={}, message_id={}, reply_markup_present={}",
                    chatId,
                    messageId,
                    replyMarkup != null
            );
        } catch (RuntimeException exception) {
            log.warn(
                    "Telegram editMessageCaption failed: chat_id={}, message_id={}, reply_markup_present={}, error_type={}",
                    chatId,
                    messageId,
                    replyMarkup != null,
                    exception.getClass().getSimpleName()
            );
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

    public void answerCallbackQuery(String callbackQueryId, String text) {
        telegramApiClient.answerCallbackQuery(callbackQueryId, text);
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
