package com.example.atlas.agent.core;

import com.example.atlas.agent.Agent;
import com.example.atlas.agent.AgentContext;
import com.example.atlas.agent.AgentResult;
import com.example.atlas.orchestrator.RequestType;
import com.example.atlas.integrations.IntegrationSettingsPort;
import com.example.atlas.privacy.PrivacyExport;
import com.example.atlas.privacy.PrivacyPanel;
import com.example.atlas.privacy.PrivacyService;
import com.example.atlas.routines.RoutinePreferencesService;
import com.example.atlas.routines.entity.RoutinePreferencesEntity;
import com.example.atlas.telegram.TelegramReplyTemplates;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreAgent implements Agent {

    private final ObjectProvider<PrivacyService> privacyService;
    private final ObjectProvider<RoutinePreferencesService> routinePreferencesService;
    private final ObjectProvider<IntegrationSettingsPort> integrationSettingsPort;

    @Autowired
    public CoreAgent(
            ObjectProvider<PrivacyService> privacyService,
            ObjectProvider<RoutinePreferencesService> routinePreferencesService,
            ObjectProvider<IntegrationSettingsPort> integrationSettingsPort
    ) {
        this.privacyService = privacyService;
        this.routinePreferencesService = routinePreferencesService;
        this.integrationSettingsPort = integrationSettingsPort;
    }

    public CoreAgent() {
        this.privacyService = null;
        this.routinePreferencesService = null;
        this.integrationSettingsPort = null;
    }

    @Override
    public String name() {
        return "ATLAS Core";
    }

    @Override
    public boolean supports(RequestType requestType) {
        return requestType == RequestType.START
                || requestType == RequestType.GENERAL
                || requestType == RequestType.PRIVACY
                || requestType == RequestType.MEMORY
                || requestType == RequestType.EXPORT
                || requestType == RequestType.FORGET
                || requestType == RequestType.DELETE_MY_DATA
                || requestType == RequestType.ROUTINES
                || requestType == RequestType.INTEGRATIONS;
    }

    @Override
    public AgentResult handle(AgentContext context) {
        String content = switch (context.requestType()) {
            case START -> TelegramReplyTemplates.startWelcome();
            case GENERAL -> TelegramReplyTemplates.generalFallback();
            case PRIVACY -> privacy(context);
            case MEMORY -> memory(context);
            case EXPORT -> export(context);
            case FORGET -> forget(context);
            case DELETE_MY_DATA -> deleteMyData(context);
            case ROUTINES -> routines(context);
            case INTEGRATIONS -> integrations(context);
            default -> "Маршрут принят ATLAS Core.";
        };

        return AgentResult.reply(content, name());
    }

    private String privacy(AgentContext context) {
        PrivacyService service = service();
        if (service == null || context.internalUserId() == null) {
            return "Privacy: ATLAS stores profile, check-ins, habits, reflections, reports, memory and Telegram identifiers. Use /export, /forget DELETE or /delete_my_data DELETE for data controls.";
        }
        PrivacyPanel panel = service.panel(context.internalUserId());
        return """
                Privacy

                Stored data:
                - profile records: %d
                - check-ins: %d
                - habits: %d
                - reflections: %d
                - memory records: %d
                - Telegram identifiers: %s

                Use /export for a data export, /forget DELETE to archive memory, or /delete_my_data DELETE to delete user-scoped data.
                """.formatted(
                panel.profileCount(),
                panel.checkInCount(),
                panel.habitCount(),
                panel.reflectionCount(),
                panel.memoryCount(),
                panel.telegramIdentifiersStored() ? "present" : "not present"
        );
    }

    private String memory(AgentContext context) {
        PrivacyService service = service();
        if (service == null || context.internalUserId() == null) {
            return "Memory: ATLAS can store user-scoped preferences, patterns and summaries. Raw sensitive content is not shown by default.";
        }
        PrivacyPanel panel = service.panel(context.internalUserId());
        return "Memory: %d active records. Raw memory content is hidden by default. Use /forget DELETE to archive memory only.".formatted(panel.memoryCount());
    }

    private String export(AgentContext context) {
        PrivacyService service = service();
        if (service == null || context.internalUserId() == null) {
            return "Export is available after persistence and Telegram user context are available.";
        }
        PrivacyExport export = service.export(context.internalUserId());
        return export.markdown() + "\n\nJSON\n" + export.json();
    }

    private String forget(AgentContext context) {
        PrivacyService service = service();
        if (service == null || context.internalUserId() == null) {
            return "Forget memory requires persisted user context.";
        }
        if (!confirmed(context)) {
            return "Forget memory requires confirmation. Send: /forget DELETE";
        }
        service.forgetMemory(context.internalUserId(), "DELETE");
        return "Memory archived for the current user.";
    }

    private String deleteMyData(AgentContext context) {
        PrivacyService service = service();
        if (service == null || context.internalUserId() == null) {
            return "Delete my data requires persisted user context.";
        }
        if (!confirmed(context)) {
            return "Delete my data is destructive and requires confirmation. Send: /delete_my_data DELETE";
        }
        service.deleteMyData(context.internalUserId(), "DELETE");
        return "User-scoped ATLAS data was deleted for the current user.";
    }

    private boolean confirmed(AgentContext context) {
        return context.message() != null && context.message().strip().endsWith("DELETE");
    }

    private PrivacyService service() {
        return privacyService == null ? null : privacyService.getIfAvailable();
    }

    private String routines(AgentContext context) {
        RoutinePreferencesService service = routinePreferencesService == null ? null : routinePreferencesService.getIfAvailable();
        if (service == null || context.user() == null) {
            return "Routines: check-in and evening reminder preferences include timezone, quiet hours and enabled state.";
        }
        RoutinePreferencesEntity preferences = service.getOrCreate(context.user());
        return """
                Routines

                Enabled: %s
                Check-in time: %s
                Evening time: %s
                Timezone: %s
                Quiet hours: %s-%s
                """.formatted(
                preferences.isEnabled(),
                preferences.getCheckinTime(),
                preferences.getEveningTime(),
                preferences.getTimezone(),
                preferences.getQuietHoursStart(),
                preferences.getQuietHoursEnd()
        );
    }

    private String integrations(AgentContext context) {
        IntegrationSettingsPort port = integrationSettingsPort == null ? null : integrationSettingsPort.getIfAvailable();
        if (port == null || context.internalUserId() == null) {
            return "Integrations: Markdown export and calendar contracts are available as foundations without external sync.";
        }
        java.util.List<com.example.atlas.integrations.IntegrationSettings> settings = port.findAll(context.internalUserId());
        if (settings.isEmpty()) {
            return "Integrations: no integrations enabled. Available foundations: Markdown export and calendar preview contract.";
        }
        String lines = settings.stream()
                .map(setting -> "- %s: %s".formatted(setting.type(), setting.status()))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        return "Integrations\n\n" + lines;
    }
}
