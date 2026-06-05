package com.example.atlas.telegram.application;

import com.example.atlas.shared.events.EventPublisher;
import com.example.atlas.shared.events.TelegramPanelRenderedEvent;
import com.example.atlas.telegram.InlineKeyboardMarkup;
import com.example.atlas.telegram.TelegramMessageSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TelegramPanelService {

    private final TelegramMessageSender messageSender;
    private final EventPublisher eventPublisher;

    public TelegramPanelService(TelegramMessageSender messageSender, EventPublisher eventPublisher) {
        this.messageSender = messageSender;
        this.eventPublisher = eventPublisher;
    }

    public void sendPanel(Long chatId, String content, InlineKeyboardMarkup replyMarkup, String panelName) {
        messageSender.sendPanel(chatId, content, replyMarkup);
        eventPublisher.publish(new TelegramPanelRenderedEvent(chatId, panelName, Instant.now()));
    }
}
