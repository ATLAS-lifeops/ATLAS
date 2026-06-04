# Telegram UX Layer

ATLAS supports both inline buttons and text commands in Telegram. Buttons are the primary user-facing path, while commands remain available for power users and text-only clients.

## Main Menu

After `/start`, ATLAS either starts onboarding or shows the main menu when the life profile is already completed.

Main menu actions:

- Check-in
- Day plan
- Habits
- Evening
- Report
- Minimal plan
- Question
- Settings

Each button uses stable callback data in the `atlas:*` namespace and routes to the same internal action as the matching command. This keeps command and button behavior consistent.

## Button Flows

Onboarding uses buttons for life area and planning style choices, with text answers still accepted as a fallback.

Daily check-in uses numeric 1-10 buttons for score questions and yes/no buttons for the overload question. Free-text questions, such as the main priority, remain text-based.

Habit tracking and evening reflection keep open answers as text input and add buttons where the choice is discrete or navigation is useful.

After day plans, reports, habit tracking and evening reflection, ATLAS includes concise next-action buttons so the user can continue without remembering commands.

## Question Entry

The Question button is a lightweight routing entry point. It does not provide full free-form assistant behavior. ATLAS explains that it currently works best through structured flows: check-in, day plan, habits, evening reflection and report.

## Settings

Settings shows concise profile state:

- onboarding completed
- primary life area
- planning style
- important life loops

Telegram tokens, webhook secrets and other sensitive runtime values are never shown in Telegram settings.

Profile restart is available through a confirmation step before onboarding is restarted.
