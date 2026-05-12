package com.example.atlas.telegram;

public final class TelegramReplyTemplates {

    private TelegramReplyTemplates() {
    }

    public static String startWelcome() {
        return "ATLAS на связи. Я помогаю держать режим, тренировки, восстановление, привычки и питание в реалистичном ритме. "
                + "Начни с /checkin, чтобы я понял состояние, или /day для плана на день.";
    }

    public static String generalFallback() {
        return "Я могу помочь с режимом, тренировкой, восстановлением, привычками и питанием. Быстрый старт: /checkin или /day.";
    }

    public static String unsupportedContent() {
        return "Я пока обрабатываю только текстовые сообщения и команды.";
    }
}
