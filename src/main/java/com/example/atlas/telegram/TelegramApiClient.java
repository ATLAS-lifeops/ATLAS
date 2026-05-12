package com.example.atlas.telegram;

public interface TelegramApiClient {

    void sendMessage(long chatId, String text);
}
