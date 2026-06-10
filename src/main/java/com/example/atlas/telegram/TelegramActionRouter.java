package com.example.atlas.telegram;

import com.example.atlas.orchestrator.RequestType;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
public class TelegramActionRouter {

    public static final String MENU = "atlas:menu";
    public static final String BACK = "atlas:back";
    public static final String CONTINUE = "atlas:continue";
    public static final String RESTART = "atlas:restart";
    public static final String CONFIRM = "atlas:confirm";
    public static final String DECLINE = "atlas:decline";
    public static final String ONBOARDING_START = "atlas:onboarding:start";
    public static final String CHECKIN_START = "atlas:checkin:start";
    public static final String DAY = "atlas:day";
    public static final String HABITS_START = "atlas:habits:start";
    public static final String EVENING_START = "atlas:evening:start";
    public static final String REPORT = "atlas:report";
    public static final String QUESTION_START = "atlas:question:start";
    public static final String SETTINGS = "atlas:settings";
    public static final String PROFILE = "atlas:profile";
    public static final String HELP = "atlas:help";
    public static final String CANCEL = "atlas:cancel";
    public static final String EMERGENCY = "atlas:emergency";
    public static final String SETTINGS_RESTART_CONFIRM = "atlas:settings:restart_confirm";
    public static final String ONBOARDING_RESTART = "atlas:onboarding:restart";
    public static final String SETTINGS_LANGUAGE = "atlas:settings:language";
    public static final String LANGUAGE_RU = "atlas:language:ru";
    public static final String LANGUAGE_EN = "atlas:language:en";
    public static final String PRIVACY = "atlas:privacy";
    public static final String ROUTINES = "atlas:routines";
    public static final String WEEKLY_PLANNING = "atlas:weekly_planning";
    public static final String INTEGRATIONS = "atlas:integrations";

    private static final Set<String> LIFE_AREAS = Set.of(
            "DAILY_STRUCTURE",
            "FOCUS",
            "HABITS",
            "ENERGY",
            "MOVEMENT",
            "NUTRITION",
            "GENERAL_BALANCE"
    );
    private static final Set<String> PLANNING_STYLES = Set.of("MINIMAL", "BALANCED", "DETAILED");
    private static final Set<String> CHECKIN_METRICS = Set.of("energy", "focus", "stress", "sleep", "mood");

    private static final Map<String, TelegramAction> CALLBACK_ACTIONS = Map.ofEntries(
            Map.entry(MENU, TelegramAction.OPEN_MAIN_MENU),
            Map.entry(BACK, TelegramAction.GO_BACK),
            Map.entry(CONTINUE, TelegramAction.CONTINUE_FLOW),
            Map.entry(RESTART, TelegramAction.RESTART_FLOW),
            Map.entry(CONFIRM, TelegramAction.CONFIRM_ACTION),
            Map.entry(DECLINE, TelegramAction.DECLINE_ACTION),
            Map.entry(ONBOARDING_START, TelegramAction.START_ONBOARDING),
            Map.entry(CHECKIN_START, TelegramAction.START_CHECKIN),
            Map.entry(DAY, TelegramAction.SHOW_DAY_PLAN),
            Map.entry(HABITS_START, TelegramAction.START_HABITS),
            Map.entry(EVENING_START, TelegramAction.START_EVENING),
            Map.entry(REPORT, TelegramAction.SHOW_WEEKLY_REPORT),
            Map.entry(QUESTION_START, TelegramAction.START_QUESTION),
            Map.entry(SETTINGS, TelegramAction.OPEN_SETTINGS),
            Map.entry(PROFILE, TelegramAction.OPEN_PROFILE),
            Map.entry(HELP, TelegramAction.SHOW_HELP),
            Map.entry(CANCEL, TelegramAction.CANCEL_FLOW),
            Map.entry(EMERGENCY, TelegramAction.SHOW_EMERGENCY_PLAN),
            Map.entry(SETTINGS_RESTART_CONFIRM, TelegramAction.CONFIRM_RESTART_ONBOARDING),
            Map.entry(ONBOARDING_RESTART, TelegramAction.RESTART_ONBOARDING),
            Map.entry(SETTINGS_LANGUAGE, TelegramAction.CHANGE_LANGUAGE),
            Map.entry(LANGUAGE_RU, TelegramAction.SELECT_LANGUAGE_RU),
            Map.entry(LANGUAGE_EN, TelegramAction.SELECT_LANGUAGE_EN),
            Map.entry(PRIVACY, TelegramAction.OPEN_PRIVACY),
            Map.entry(ROUTINES, TelegramAction.OPEN_ROUTINES),
            Map.entry(WEEKLY_PLANNING, TelegramAction.OPEN_WEEKLY_PLANNING),
            Map.entry(INTEGRATIONS, TelegramAction.OPEN_INTEGRATIONS)
    );

    public Optional<TelegramAction> actionForCommand(String text, boolean onboardingCompleted) {
        if (text == null || text.isBlank() || !text.strip().startsWith("/")) {
            return Optional.empty();
        }
        String command = text.strip().toLowerCase(Locale.ROOT).split("\\s+", 2)[0];
        return switch (command) {
            case "/start" -> Optional.of(onboardingCompleted ? TelegramAction.OPEN_MAIN_MENU : TelegramAction.START_ONBOARDING);
            case "/checkin" -> Optional.of(TelegramAction.START_CHECKIN);
            case "/day" -> Optional.of(TelegramAction.SHOW_DAY_PLAN);
            case "/habits" -> Optional.of(TelegramAction.START_HABITS);
            case "/evening", "/review" -> Optional.of(TelegramAction.START_EVENING);
            case "/report" -> Optional.of(TelegramAction.SHOW_WEEKLY_REPORT);
            case "/help" -> Optional.of(TelegramAction.SHOW_HELP);
            case "/cancel" -> Optional.of(TelegramAction.CANCEL_FLOW);
            case "/emergency" -> Optional.of(TelegramAction.SHOW_EMERGENCY_PLAN);
            default -> Optional.empty();
        };
    }

    public Optional<TelegramAction> actionForCallback(String callbackData) {
        return Optional.ofNullable(CALLBACK_ACTIONS.get(callbackData));
    }

    public Optional<String> flowInputForCallback(String callbackData) {
        String value = normalize(callbackData);
        if (value.startsWith("atlas:onboarding:life_area:")) {
            return allowedSuffix(value, "atlas:onboarding:life_area:", LIFE_AREAS);
        }
        if (value.startsWith("atlas:onboarding:planning_style:")) {
            return allowedSuffix(value, "atlas:onboarding:planning_style:", PLANNING_STYLES);
        }
        if (value.startsWith("atlas:checkin:value:")) {
            String[] parts = value.split(":");
            if (parts.length == 5 && CHECKIN_METRICS.contains(parts[3]) && score(parts[4])) {
                return Optional.of(parts[4]);
            }
        }
        if (value.equals("atlas:checkin:overload:yes") || value.equals("atlas:habit:completed:yes")) {
            return Optional.of("да");
        }
        if (value.equals("atlas:checkin:overload:no") || value.equals("atlas:habit:completed:no")) {
            return Optional.of("нет");
        }
        return Optional.empty();
    }

    public RequestType requestType(TelegramAction action) {
        return switch (action) {
            case START_ONBOARDING, RESTART_ONBOARDING -> RequestType.START;
            case START_CHECKIN -> RequestType.CHECKIN;
            case SHOW_DAY_PLAN -> RequestType.DAY_PLAN;
            case START_HABITS -> RequestType.HABITS;
            case START_EVENING -> RequestType.EVENING_REFLECTION;
            case SHOW_WEEKLY_REPORT -> RequestType.REPORT;
            case SHOW_HELP -> RequestType.HELP;
            case CANCEL_FLOW -> RequestType.CANCEL;
            case SHOW_EMERGENCY_PLAN -> RequestType.EMERGENCY;
            case OPEN_MAIN_MENU, GO_BACK, CONTINUE_FLOW, RESTART_FLOW, CONFIRM_ACTION, DECLINE_ACTION,
                    START_QUESTION, OPEN_SETTINGS, OPEN_PROFILE, CONFIRM_RESTART_ONBOARDING, CHANGE_LANGUAGE,
                    SELECT_LANGUAGE_RU, SELECT_LANGUAGE_EN -> RequestType.GENERAL;
            case OPEN_PRIVACY -> RequestType.PRIVACY;
            case OPEN_ROUTINES -> RequestType.ROUTINES;
            case OPEN_WEEKLY_PLANNING -> RequestType.WEEK_PLAN;
            case OPEN_INTEGRATIONS -> RequestType.INTEGRATIONS;
        };
    }

    public String commandForAction(TelegramAction action) {
        return switch (action) {
            case START_ONBOARDING, RESTART_ONBOARDING -> "/start";
            case START_CHECKIN -> "/checkin";
            case SHOW_DAY_PLAN -> "/day";
            case START_HABITS -> "/habits";
            case START_EVENING -> "/evening";
            case SHOW_WEEKLY_REPORT -> "/report";
            case SHOW_HELP -> "/help";
            case CANCEL_FLOW -> "/cancel";
            case SHOW_EMERGENCY_PLAN -> "/emergency";
            case OPEN_MAIN_MENU, GO_BACK, CONTINUE_FLOW, RESTART_FLOW, CONFIRM_ACTION, DECLINE_ACTION,
                    START_QUESTION, OPEN_SETTINGS, OPEN_PROFILE, CONFIRM_RESTART_ONBOARDING, CHANGE_LANGUAGE,
                    SELECT_LANGUAGE_RU, SELECT_LANGUAGE_EN -> "";
            case OPEN_PRIVACY -> "/privacy";
            case OPEN_ROUTINES -> "/routines";
            case OPEN_WEEKLY_PLANNING -> "/week";
            case OPEN_INTEGRATIONS -> "/integrations";
        };
    }

    public boolean isSupportedCallback(String callbackData) {
        return actionForCallback(callbackData).isPresent() || flowInputForCallback(callbackData).isPresent();
    }

    private Optional<String> allowedSuffix(String value, String prefix, Set<String> allowedValues) {
        String suffix = value.substring(prefix.length());
        return allowedValues.contains(suffix) ? Optional.of(suffix) : Optional.empty();
    }

    private boolean score(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed >= 1 && parsed <= 10;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip();
    }
}
