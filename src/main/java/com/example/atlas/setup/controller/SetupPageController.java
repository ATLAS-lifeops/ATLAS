package com.example.atlas.setup.controller;

import com.example.atlas.runtime.service.AtlasRuntimeSettingsService;
import com.example.atlas.runtime.service.RuntimeSettingsStatus;
import com.example.atlas.runtime.entity.TelegramLaunchMode;
import com.example.atlas.runtime.service.RuntimeSettingsValidationException;
import com.example.atlas.setup.dto.SetupSubmissionRequest;
import com.example.atlas.setup.service.TelegramBotIdentity;
import com.example.atlas.setup.service.TelegramBotTokenValidationException;
import com.example.atlas.setup.service.TelegramBotTokenValidator;
import com.example.atlas.telegram.TelegramWebhookRegistrationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ConditionalOnBean(AtlasRuntimeSettingsService.class)
public class SetupPageController {

    private final AtlasRuntimeSettingsService runtimeSettingsService;
    private final TelegramBotTokenValidator tokenValidator;
    private final ObjectProvider<TelegramWebhookRegistrationService> webhookRegistrationService;

    public SetupPageController(
            AtlasRuntimeSettingsService runtimeSettingsService,
            TelegramBotTokenValidator tokenValidator,
            ObjectProvider<TelegramWebhookRegistrationService> webhookRegistrationService
    ) {
        this.runtimeSettingsService = runtimeSettingsService;
        this.tokenValidator = tokenValidator;
        this.webhookRegistrationService = webhookRegistrationService;
    }

    @GetMapping("/")
    public ResponseEntity<Void> index() {
        String location = runtimeSettingsService.isSetupCompleted() ? "/setup/status" : "/setup";
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, location)
                .build();
    }

    @GetMapping("/setup")
    public ResponseEntity<String> setup() {
        return html(setupPage(null, runtimeSettingsService.status()));
    }

    @PostMapping("/setup")
    public ResponseEntity<String> submitSetup(@ModelAttribute SetupSubmissionRequest request) {
        try {
            TelegramLaunchMode mode = TelegramLaunchMode.valueOf(request.mode());
            TelegramBotIdentity identity = tokenValidator.validate(request.botToken());
            String botUsername = firstNonBlank(request.botUsername(), identity.username());
            runtimeSettingsService.saveTelegramSetup(
                    request.botToken(),
                    botUsername,
                    mode,
                    request.publicBaseUrl(),
                    request.webhookSecret()
            );
            if (mode == TelegramLaunchMode.WEBHOOK) {
                TelegramWebhookRegistrationService service = webhookRegistrationService.getIfAvailable();
                if (service != null) {
                    service.registerConfiguredWebhook();
                }
            }
            return html(successPage(runtimeSettingsService.status()));
        } catch (IllegalArgumentException exception) {
            return html(setupPage("Launch mode must be Simple local launch or Production webhook.", runtimeSettingsService.status()));
        } catch (IllegalStateException exception) {
            return html(setupPage(exception.getMessage(), runtimeSettingsService.status()));
        } catch (RuntimeSettingsValidationException | TelegramBotTokenValidationException exception) {
            return html(setupPage(exception.getMessage(), runtimeSettingsService.status()));
        }
    }

    @GetMapping("/setup/status")
    @ResponseBody
    public RuntimeSettingsStatus setupStatus() {
        return runtimeSettingsService.status();
    }

    public static ResponseEntity<String> html(String body) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(body);
    }

    public static String setupPage(String message, RuntimeSettingsStatus status) {
        String error = message == null || message.isBlank()
                ? ""
                : "<div class=\"error\">" + escape(message) + "</div>";
        String completed = status != null && status.setupCompleted()
                ? "<div class=\"success\">Setup is completed. You can update the settings below if needed.</div>"
                : "";

        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>ATLAS Setup</title>
                  <style>
                    body { margin: 0; font-family: Arial, sans-serif; background: #f4f6f8; color: #17202a; }
                    main { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
                    section { width: min(100%%, 520px); background: #fff; border: 1px solid #d8dee4; border-radius: 8px; padding: 28px; box-shadow: 0 8px 28px rgba(23,32,42,.08); }
                    h1 { margin: 0 0 8px; font-size: 28px; letter-spacing: 0; }
                    p { margin: 0 0 20px; color: #536471; line-height: 1.5; }
                    label { display: block; margin: 16px 0 6px; font-weight: 700; }
                    input, select { box-sizing: border-box; width: 100%%; padding: 11px 12px; border: 1px solid #c8d0d8; border-radius: 6px; font: inherit; }
                    button { margin-top: 22px; width: 100%%; border: 0; border-radius: 6px; padding: 12px 14px; background: #1565c0; color: #fff; font-weight: 700; font: inherit; cursor: pointer; }
                    button:hover { background: #0f559f; }
                    .error { margin: 0 0 16px; padding: 12px; border-radius: 6px; background: #fff1f0; color: #a8071a; border: 1px solid #ffa39e; }
                    .success { margin: 0 0 16px; padding: 12px; border-radius: 6px; background: #f6ffed; color: #237804; border: 1px solid #b7eb8f; }
                    .hint { margin-top: 6px; font-size: 13px; color: #6b7785; }
                    .webhook-fields { display: none; }
                    .webhook-fields.visible { display: block; }
                  </style>
                </head>
                <body>
                  <main>
                    <section>
                      <h1>ATLAS</h1>
                      <p>Connect your Telegram bot and choose how ATLAS should receive Telegram updates.</p>
                      %s
                      %s
                      <form method="post" action="/setup">
                        <label for="botToken">Telegram Bot Token</label>
                        <input id="botToken" name="botToken" type="password" autocomplete="off" required>
                        <div class="hint">Paste the token from BotFather. It will not be shown again.</div>

                        <label for="botUsername">Telegram Bot Username</label>
                        <input id="botUsername" name="botUsername" type="text" autocomplete="off" placeholder="Optional">

                        <label for="mode">Launch mode</label>
                        <select id="mode" name="mode">
                          <option value="POLLING">Simple local launch</option>
                          <option value="WEBHOOK">Production webhook</option>
                        </select>

                        <div id="webhookFields" class="webhook-fields">
                          <label for="publicBaseUrl">Public Base URL</label>
                          <input id="publicBaseUrl" name="publicBaseUrl" type="url" placeholder="https://example.com">

                          <label for="webhookSecret">Webhook Secret</label>
                          <input id="webhookSecret" name="webhookSecret" type="password" autocomplete="off">
                        </div>

                        <button type="submit">Save setup</button>
                      </form>
                    </section>
                  </main>
                  <script>
                    const mode = document.getElementById('mode');
                    const fields = document.getElementById('webhookFields');
                    function syncWebhookFields() {
                      fields.classList.toggle('visible', mode.value === 'WEBHOOK');
                    }
                    mode.addEventListener('change', syncWebhookFields);
                    syncWebhookFields();
                  </script>
                </body>
                </html>
                """.formatted(completed, error);
    }

    public static String successPage(RuntimeSettingsStatus status) {
        String mode = status.telegramMode() == null ? "Not configured" : status.telegramMode().name();
        String username = status.botUsername() == null ? "Not configured" : escape(status.botUsername());
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>ATLAS Setup Saved</title>
                  <style>
                    body { margin: 0; font-family: Arial, sans-serif; background: #f4f6f8; color: #17202a; }
                    main { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
                    section { width: min(100%%, 520px); background: #fff; border: 1px solid #d8dee4; border-radius: 8px; padding: 28px; box-shadow: 0 8px 28px rgba(23,32,42,.08); }
                    h1 { margin: 0 0 8px; font-size: 28px; letter-spacing: 0; }
                    p { color: #536471; line-height: 1.5; }
                    dl { display: grid; grid-template-columns: 150px 1fr; gap: 10px; }
                    dt { font-weight: 700; }
                    a { color: #1565c0; }
                  </style>
                </head>
                <body>
                  <main>
                    <section>
                      <h1>Setup saved</h1>
                      <p>ATLAS saved the Telegram setup. Open Telegram and send <strong>/start</strong> to your bot.</p>
                      <dl>
                        <dt>Mode</dt><dd>%s</dd>
                        <dt>Bot username</dt><dd>%s</dd>
                        <dt>Token</dt><dd>%s</dd>
                        <dt>Webhook</dt><dd>%s</dd>
                      </dl>
                      <p><a href="/setup">Edit setup</a></p>
                    </section>
                  </main>
                </body>
                </html>
                """.formatted(
                escape(mode),
                username,
                status.tokenConfigured() ? "Configured" : "Not configured",
                status.webhookConfigured() ? "Configured" : "Not configured"
        );
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.strip();
        }
        return second == null || second.isBlank() ? null : second.strip();
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
