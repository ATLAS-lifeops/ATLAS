package com.example.atlas.telegram;

public final class TelegramReplyTemplates {

    private TelegramReplyTemplates() {
    }

    public static String startWelcome() {
        return "ATLAS на связи. Я помогаю видеть состояние, держать фокус, отслеживать привычки "
                + "и возвращать день в управляемый ритм. Начни с /start или /checkin.";
    }

    public static String generalFallback() {
        return "Я могу помочь собрать день в систему: /checkin, /day, /habits, /evening или /report.";
    }

    public static String unsupportedContent() {
        return "Я пока обрабатываю только текстовые сообщения и команды.";
    }
}
