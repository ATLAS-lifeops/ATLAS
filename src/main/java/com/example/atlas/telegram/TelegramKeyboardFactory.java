package com.example.atlas.telegram;

import com.example.atlas.conversation.ConversationFlowType;
import com.example.atlas.conversation.entity.ConversationStateEntity;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.user.UserLanguage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramKeyboardFactory {

    public InlineKeyboardMarkup mainMenu() {
        return mainMenu(UserLanguage.RU);
    }

    public InlineKeyboardMarkup mainMenu(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🗓 Day plan", TelegramActionRouter.DAY)),
                    row(button("✅ Habits", TelegramActionRouter.HABITS_START), button("🌙 Evening", TelegramActionRouter.EVENING_START)),
                    row(button("📊 Report", TelegramActionRouter.REPORT), button("🚨 Minimal plan", TelegramActionRouter.EMERGENCY)),
                    row(button("❓ Question", TelegramActionRouter.QUESTION_START), button("⚙️ Settings", TelegramActionRouter.SETTINGS))
            );
        }
        return keyboard(
                row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🗓 План дня", TelegramActionRouter.DAY)),
                row(button("✅ Привычки", TelegramActionRouter.HABITS_START), button("🌙 Вечер", TelegramActionRouter.EVENING_START)),
                row(button("📊 Отчёт", TelegramActionRouter.REPORT), button("🚨 Минимальный план", TelegramActionRouter.EMERGENCY)),
                row(button("❓ Вопрос", TelegramActionRouter.QUESTION_START), button("⚙️ Настройки", TelegramActionRouter.SETTINGS))
        );
    }

    public InlineKeyboardMarkup languageSelection() {
        return keyboard(
                row(button("🇷🇺 Русский", TelegramActionRouter.LANGUAGE_RU), button("🇬🇧 English", TelegramActionRouter.LANGUAGE_EN))
        );
    }

    public InlineKeyboardMarkup help() {
        return keyboard(row(button("Меню", TelegramActionRouter.MENU)));
    }

    public InlineKeyboardMarkup onboardingLifeAreas() {
        return keyboard(
                row(button("Режим дня", "atlas:onboarding:life_area:DAILY_STRUCTURE"), button("Фокус и задачи", "atlas:onboarding:life_area:FOCUS")),
                row(button("Привычки", "atlas:onboarding:life_area:HABITS"), button("Энергия", "atlas:onboarding:life_area:ENERGY")),
                row(button("Движение", "atlas:onboarding:life_area:MOVEMENT"), button("Питание", "atlas:onboarding:life_area:NUTRITION")),
                row(button("Баланс", "atlas:onboarding:life_area:GENERAL_BALANCE")),
                row(button("Отмена", TelegramActionRouter.CANCEL))
        );
    }

    public InlineKeyboardMarkup planningStyles() {
        return keyboard(
                row(button("Минимальный", "atlas:onboarding:planning_style:MINIMAL")),
                row(button("Сбалансированный", "atlas:onboarding:planning_style:BALANCED")),
                row(button("Подробный", "atlas:onboarding:planning_style:DETAILED")),
                row(button("Отмена", TelegramActionRouter.CANCEL))
        );
    }

    public InlineKeyboardMarkup score(String metric) {
        return keyboard(
                row(scoreButton(metric, 1), scoreButton(metric, 2), scoreButton(metric, 3), scoreButton(metric, 4), scoreButton(metric, 5)),
                row(scoreButton(metric, 6), scoreButton(metric, 7), scoreButton(metric, 8), scoreButton(metric, 9), scoreButton(metric, 10)),
                row(button("Отмена", TelegramActionRouter.CANCEL))
        );
    }

    public InlineKeyboardMarkup yesNo(String yesCallback, String noCallback) {
        return keyboard(
                row(button("Нет", noCallback), button("Да", yesCallback)),
                row(button("Отмена", TelegramActionRouter.CANCEL))
        );
    }

    public InlineKeyboardMarkup backToMenu() {
        return keyboard(row(button("Меню", TelegramActionRouter.MENU)));
    }

    public InlineKeyboardMarkup backToMenu(UserLanguage language) {
        return language == UserLanguage.EN
                ? keyboard(row(button("Menu", TelegramActionRouter.MENU)))
                : backToMenu();
    }

    public InlineKeyboardMarkup dayPlanActions() {
        return keyboard(
                row(button("✅ Привычка", TelegramActionRouter.HABITS_START), button("🌙 Вечер", TelegramActionRouter.EVENING_START)),
                row(button("🚨 Минимальный план", TelegramActionRouter.EMERGENCY), button("Меню", TelegramActionRouter.MENU))
        );
    }

    public InlineKeyboardMarkup reportActions() {
        return keyboard(
                row(button("🌅 Новый check-in", TelegramActionRouter.CHECKIN_START), button("🗓 План дня", TelegramActionRouter.DAY)),
                row(button("Меню", TelegramActionRouter.MENU))
        );
    }

    public InlineKeyboardMarkup habitCompleteActions() {
        return keyboard(
                row(button("Меню", TelegramActionRouter.MENU), button("🌙 Вечер", TelegramActionRouter.EVENING_START)),
                row(button("📊 Отчёт", TelegramActionRouter.REPORT))
        );
    }

    public InlineKeyboardMarkup eveningCompleteActions() {
        return keyboard(
                row(button("Меню", TelegramActionRouter.MENU), button("🗓 План дня", TelegramActionRouter.DAY)),
                row(button("📊 Отчёт", TelegramActionRouter.REPORT))
        );
    }

    public InlineKeyboardMarkup questionActions() {
        return keyboard(
                row(button("Check-in", TelegramActionRouter.CHECKIN_START), button("План дня", TelegramActionRouter.DAY)),
                row(button("Привычки", TelegramActionRouter.HABITS_START), button("Отчёт", TelegramActionRouter.REPORT)),
                row(button("Меню", TelegramActionRouter.MENU))
        );
    }

    public InlineKeyboardMarkup settingsActions() {
        return settingsActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup settingsActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌐 Change language", TelegramActionRouter.SETTINGS_LANGUAGE)),
                    row(button("🔄 Restart onboarding", TelegramActionRouter.SETTINGS_RESTART_CONFIRM)),
                    row(button("⬅️ Menu", TelegramActionRouter.MENU))
            );
        }
        return keyboard(
                row(button("🌐 Изменить язык", TelegramActionRouter.SETTINGS_LANGUAGE)),
                row(button("🔄 Перезапустить onboarding", TelegramActionRouter.SETTINGS_RESTART_CONFIRM)),
                row(button("⬅️ Меню", TelegramActionRouter.MENU))
        );
    }

    public InlineKeyboardMarkup restartConfirmation() {
        return keyboard(
                row(button("Да, перезапустить", TelegramActionRouter.ONBOARDING_RESTART), button("Отмена", TelegramActionRouter.MENU))
        );
    }

    public InlineKeyboardMarkup forRequest(RequestType requestType) {
        return switch (requestType) {
            case DAY_PLAN -> dayPlanActions();
            case REPORT -> reportActions();
            case HELP -> help();
            case EMERGENCY, CANCEL -> backToMenu();
            default -> null;
        };
    }

    public InlineKeyboardMarkup forActiveStep(ConversationStateEntity state) {
        if (state == null) {
            return null;
        }
        if (state.getFlowType() == ConversationFlowType.ONBOARDING) {
            return switch (state.getStep()) {
                case "ASK_PRIMARY_LIFE_AREA" -> onboardingLifeAreas();
                case "ASK_PLANNING_STYLE" -> planningStyles();
                default -> keyboard(row(button("Отмена", TelegramActionRouter.CANCEL)));
            };
        }
        if (state.getFlowType() == ConversationFlowType.DAILY_CHECKIN) {
            return switch (state.getStep()) {
                case "ASK_ENERGY" -> score("energy");
                case "ASK_FOCUS" -> score("focus");
                case "ASK_STRESS" -> score("stress");
                case "ASK_SLEEP" -> score("sleep");
                case "ASK_MOOD" -> score("mood");
                case "ASK_OVERLOAD_SIGNAL" -> yesNo("atlas:checkin:overload:yes", "atlas:checkin:overload:no");
                default -> keyboard(row(button("Отмена", TelegramActionRouter.CANCEL)));
            };
        }
        if (state.getFlowType() == ConversationFlowType.HABIT_TRACKING) {
            if ("ASK_COMPLETION".equals(state.getStep())) {
                return yesNo("atlas:habit:completed:yes", "atlas:habit:completed:no");
            }
            return keyboard(row(button("Отмена", TelegramActionRouter.CANCEL)));
        }
        if (state.getFlowType() == ConversationFlowType.EVENING_REFLECTION) {
            return keyboard(row(button("Отмена", TelegramActionRouter.CANCEL)));
        }
        return null;
    }

    private InlineKeyboardButton scoreButton(String metric, int value) {
        return button(Integer.toString(value), "atlas:checkin:value:" + metric + ":" + value);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return new InlineKeyboardButton(text, callbackData);
    }

    @SafeVarargs
    private final InlineKeyboardMarkup keyboard(List<InlineKeyboardButton>... rows) {
        return new InlineKeyboardMarkup(List.of(rows));
    }

    private List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return new ArrayList<>(List.of(buttons));
    }
}
