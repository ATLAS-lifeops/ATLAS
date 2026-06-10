# Integrations

The integration foundation is port-first:

- integration settings persist safe metadata only through `IntegrationSettingsPort`;
- Markdown export is user-scoped;
- calendar integration exposes a preview contract;
- no OAuth flow or full external sync is included.

The Telegram settings panel links to integrations status. Future integrations can implement these ports without changing ATLAS into a multi-service system.
