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
        return help(UserLanguage.RU);
    }

    public InlineKeyboardMarkup help(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🗓 Day plan", TelegramActionRouter.DAY)),
                    row(button("✅ Habits", TelegramActionRouter.HABITS_START), button("📊 Report", TelegramActionRouter.REPORT)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🗓 План дня", TelegramActionRouter.DAY)),
                row(button("✅ Привычки", TelegramActionRouter.HABITS_START), button("📊 Отчёт", TelegramActionRouter.REPORT)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup onboardingLifeAreas() {
        return onboardingLifeAreas(UserLanguage.RU);
    }

    public InlineKeyboardMarkup onboardingLifeAreas(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("Daily rhythm", "atlas:onboarding:life_area:DAILY_STRUCTURE"), button("Focus and tasks", "atlas:onboarding:life_area:FOCUS")),
                    row(button("Habits", "atlas:onboarding:life_area:HABITS"), button("Energy", "atlas:onboarding:life_area:ENERGY")),
                    row(button("Movement", "atlas:onboarding:life_area:MOVEMENT"), button("Nutrition", "atlas:onboarding:life_area:NUTRITION")),
                    row(button("Balance", "atlas:onboarding:life_area:GENERAL_BALANCE")),
                    navigationRow(language, false, true, true)
            );
        }
        return keyboard(
                row(button("Режим дня", "atlas:onboarding:life_area:DAILY_STRUCTURE"), button("Фокус и задачи", "atlas:onboarding:life_area:FOCUS")),
                row(button("Привычки", "atlas:onboarding:life_area:HABITS"), button("Энергия", "atlas:onboarding:life_area:ENERGY")),
                row(button("Движение", "atlas:onboarding:life_area:MOVEMENT"), button("Питание", "atlas:onboarding:life_area:NUTRITION")),
                row(button("Баланс", "atlas:onboarding:life_area:GENERAL_BALANCE")),
                navigationRow(language, false, true, true)
        );
    }

    public InlineKeyboardMarkup planningStyles() {
        return planningStyles(UserLanguage.RU);
    }

    public InlineKeyboardMarkup planningStyles(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("Minimal", "atlas:onboarding:planning_style:MINIMAL")),
                    row(button("Balanced", "atlas:onboarding:planning_style:BALANCED")),
                    row(button("Detailed", "atlas:onboarding:planning_style:DETAILED")),
                    navigationRow(language, true, true, true)
            );
        }
        return keyboard(
                row(button("Минимальный", "atlas:onboarding:planning_style:MINIMAL")),
                row(button("Сбалансированный", "atlas:onboarding:planning_style:BALANCED")),
                row(button("Подробный", "atlas:onboarding:planning_style:DETAILED")),
                navigationRow(language, true, true, true)
        );
    }

    public InlineKeyboardMarkup score(String metric) {
        return score(metric, UserLanguage.RU, !"energy".equals(metric));
    }

    public InlineKeyboardMarkup score(String metric, UserLanguage language, boolean backAvailable) {
        return keyboard(
                row(scoreButton(metric, 1), scoreButton(metric, 2), scoreButton(metric, 3), scoreButton(metric, 4), scoreButton(metric, 5)),
                row(scoreButton(metric, 6), scoreButton(metric, 7), scoreButton(metric, 8), scoreButton(metric, 9), scoreButton(metric, 10)),
                navigationRow(language, backAvailable, true, true)
        );
    }

    public InlineKeyboardMarkup yesNo(String yesCallback, String noCallback) {
        return yesNo(yesCallback, noCallback, UserLanguage.RU, true);
    }

    public InlineKeyboardMarkup yesNo(String yesCallback, String noCallback, UserLanguage language, boolean backAvailable) {
        return keyboard(
                language == UserLanguage.EN
                        ? row(button("No", noCallback), button("Yes", yesCallback))
                        : row(button("Нет", noCallback), button("Да", yesCallback)),
                navigationRow(language, backAvailable, true, true)
        );
    }

    public InlineKeyboardMarkup backToMenu() {
        return keyboard(row(button("Меню", TelegramActionRouter.MENU)));
    }

    public InlineKeyboardMarkup backToMenu(UserLanguage language) {
        return language == UserLanguage.EN
                ? keyboard(row(menuButton(language)))
                : keyboard(row(menuButton(language)));
    }

    public InlineKeyboardMarkup dayPlanActions() {
        return dayPlanActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup dayPlanActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("✅ Habits", TelegramActionRouter.HABITS_START), button("🌙 Evening", TelegramActionRouter.EVENING_START)),
                    row(button("🚨 Minimal plan", TelegramActionRouter.EMERGENCY), menuButton(language))
            );
        }
        return keyboard(
                row(button("✅ Привычки", TelegramActionRouter.HABITS_START), button("🌙 Вечер", TelegramActionRouter.EVENING_START)),
                row(button("🚨 Минимальный план", TelegramActionRouter.EMERGENCY), menuButton(language))
        );
    }

    public InlineKeyboardMarkup reportActions() {
        return reportActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup reportActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌅 New check-in", TelegramActionRouter.CHECKIN_START), button("🗓 Day plan", TelegramActionRouter.DAY)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("🌅 Новый check-in", TelegramActionRouter.CHECKIN_START), button("🗓 План дня", TelegramActionRouter.DAY)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup habitCompleteActions() {
        return habitCompleteActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup habitCompleteActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌙 Evening", TelegramActionRouter.EVENING_START), button("📊 Report", TelegramActionRouter.REPORT)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("🌙 Вечер", TelegramActionRouter.EVENING_START), button("📊 Отчёт", TelegramActionRouter.REPORT)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup eveningCompleteActions() {
        return eveningCompleteActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup eveningCompleteActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("📊 Report", TelegramActionRouter.REPORT), button("🌅 Check-in", TelegramActionRouter.CHECKIN_START)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("📊 Отчёт", TelegramActionRouter.REPORT), button("🌅 Check-in", TelegramActionRouter.CHECKIN_START)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup questionActions() {
        return questionActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup questionActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("Check-in", TelegramActionRouter.CHECKIN_START), button("Day plan", TelegramActionRouter.DAY)),
                    row(button("Habits", TelegramActionRouter.HABITS_START), button("Report", TelegramActionRouter.REPORT)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("Check-in", TelegramActionRouter.CHECKIN_START), button("План дня", TelegramActionRouter.DAY)),
                row(button("Привычки", TelegramActionRouter.HABITS_START), button("Отчёт", TelegramActionRouter.REPORT)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup settingsActions() {
        return settingsActions(UserLanguage.RU);
    }

    public InlineKeyboardMarkup settingsActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌐 Change language", TelegramActionRouter.SETTINGS_LANGUAGE)),
                    row(button("🧭 Profile", TelegramActionRouter.PROFILE)),
                    row(button("🔄 Restart onboarding", TelegramActionRouter.SETTINGS_RESTART_CONFIRM)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("🌐 Изменить язык", TelegramActionRouter.SETTINGS_LANGUAGE)),
                row(button("🧭 Профиль", TelegramActionRouter.PROFILE)),
                row(button("🔄 Перезапустить onboarding", TelegramActionRouter.SETTINGS_RESTART_CONFIRM)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup restartConfirmation() {
        return restartConfirmation(UserLanguage.RU);
    }

    public InlineKeyboardMarkup restartConfirmation(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(row(button("Yes, restart", TelegramActionRouter.ONBOARDING_RESTART), cancelButton(language)));
        }
        return keyboard(row(button("Да, перезапустить", TelegramActionRouter.ONBOARDING_RESTART), cancelButton(language)));
    }

    public InlineKeyboardMarkup activeFlowContinuation(UserLanguage language) {
        return keyboard(
                row(continueButton(language), restartButton(language)),
                row(cancelButton(language), menuButton(language))
        );
    }

    public InlineKeyboardMarkup restartActiveFlowConfirmation(UserLanguage language) {
        return keyboard(row(confirmButton(language), declineButton(language)));
    }

    public InlineKeyboardMarkup onboardingCompleteActions(UserLanguage language) {
        return keyboard(row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), menuButton(language)));
    }

    public InlineKeyboardMarkup checkinCompleteActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🗓 Day plan", TelegramActionRouter.DAY), button("✅ Habits", TelegramActionRouter.HABITS_START)),
                    row(button("🚨 Minimal plan", TelegramActionRouter.EMERGENCY), menuButton(language))
            );
        }
        return keyboard(
                row(button("🗓 План дня", TelegramActionRouter.DAY), button("✅ Привычки", TelegramActionRouter.HABITS_START)),
                row(button("🚨 Минимальный план", TelegramActionRouter.EMERGENCY), menuButton(language))
        );
    }

    public InlineKeyboardMarkup dayPlanEmptyStateActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(
                    row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🚨 Minimal plan", TelegramActionRouter.EMERGENCY)),
                    row(menuButton(language))
            );
        }
        return keyboard(
                row(button("🌅 Check-in", TelegramActionRouter.CHECKIN_START), button("🚨 Минимальный план", TelegramActionRouter.EMERGENCY)),
                row(menuButton(language))
        );
    }

    public InlineKeyboardMarkup habitEmptyStateActions(UserLanguage language) {
        if (language == UserLanguage.EN) {
            return keyboard(row(button("✅ Start habit", TelegramActionRouter.HABITS_START), menuButton(language)));
        }
        return keyboard(row(button("✅ Начать привычку", TelegramActionRouter.HABITS_START), menuButton(language)));
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
        return forActiveStep(state, UserLanguage.RU);
    }

    public InlineKeyboardMarkup forActiveStep(ConversationStateEntity state, UserLanguage language) {
        if (state == null) {
            return null;
        }
        if (state.getFlowType() == ConversationFlowType.ONBOARDING) {
            return switch (state.getStep()) {
                case "ASK_PRIMARY_LIFE_AREA" -> onboardingLifeAreas(language);
                case "ASK_PLANNING_STYLE" -> planningStyles(language);
                case "ASK_CURRENT_FOCUS", "ASK_MAIN_LOOPS" -> keyboard(navigationRow(language, true, true, true));
                default -> keyboard(navigationRow(language, false, true, true));
            };
        }
        if (state.getFlowType() == ConversationFlowType.DAILY_CHECKIN) {
            return switch (state.getStep()) {
                case "ASK_ENERGY" -> score("energy", language, false);
                case "ASK_FOCUS" -> score("focus", language, true);
                case "ASK_STRESS" -> score("stress", language, true);
                case "ASK_SLEEP" -> score("sleep", language, true);
                case "ASK_MOOD" -> score("mood", language, true);
                case "ASK_OVERLOAD_SIGNAL" -> yesNo("atlas:checkin:overload:yes", "atlas:checkin:overload:no", language, true);
                default -> keyboard(navigationRow(language, true, true, true));
            };
        }
        if (state.getFlowType() == ConversationFlowType.HABIT_TRACKING) {
            if ("ASK_COMPLETION".equals(state.getStep())) {
                return yesNo("atlas:habit:completed:yes", "atlas:habit:completed:no", language, true);
            }
            return keyboard(navigationRow(language, !"ASK_HABIT".equals(state.getStep()), true, true));
        }
        if (state.getFlowType() == ConversationFlowType.EVENING_REFLECTION) {
            return keyboard(navigationRow(language, !"ASK_MAIN_RESULT".equals(state.getStep()), true, true));
        }
        return null;
    }

    private InlineKeyboardButton scoreButton(String metric, int value) {
        return button(Integer.toString(value), "atlas:checkin:value:" + metric + ":" + value);
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        return new InlineKeyboardButton(text, callbackData);
    }

    private InlineKeyboardButton backButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "⬅️ Back" : "⬅️ Назад", TelegramActionRouter.BACK);
    }

    private InlineKeyboardButton menuButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "🏠 Menu" : "🏠 Меню", TelegramActionRouter.MENU);
    }

    private InlineKeyboardButton cancelButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "✖️ Cancel" : "✖️ Отменить", TelegramActionRouter.CANCEL);
    }

    private InlineKeyboardButton continueButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "▶️ Continue" : "▶️ Продолжить", TelegramActionRouter.CONTINUE);
    }

    private InlineKeyboardButton restartButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "🔄 Restart" : "🔄 Начать заново", TelegramActionRouter.RESTART);
    }

    private InlineKeyboardButton confirmButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "Yes, restart" : "Да, перезапустить", TelegramActionRouter.CONFIRM);
    }

    private InlineKeyboardButton declineButton(UserLanguage language) {
        return button(language == UserLanguage.EN ? "Cancel" : "Отмена", TelegramActionRouter.DECLINE);
    }

    private List<InlineKeyboardButton> navigationRow(UserLanguage language, boolean back, boolean cancel, boolean menu) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if (back) {
            buttons.add(backButton(language));
        }
        if (cancel) {
            buttons.add(cancelButton(language));
        }
        if (menu) {
            buttons.add(menuButton(language));
        }
        return buttons;
    }

    @SafeVarargs
    private final InlineKeyboardMarkup keyboard(List<InlineKeyboardButton>... rows) {
        return new InlineKeyboardMarkup(List.of(rows));
    }

    private List<InlineKeyboardButton> row(InlineKeyboardButton... buttons) {
        return new ArrayList<>(List.of(buttons));
    }
}
