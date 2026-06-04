# Telegram UX

ATLAS is a button-first Telegram product. Commands still work as a fallback for power users, but the primary path is inline buttons.

## First Launch

If the user language is missing, `/start` first asks for language. After language selection, the user enters onboarding or the main menu.

## Main Menu

Main actions:

- Check-in
- Day plan
- Habits
- Evening
- Report
- Minimal plan
- Question
- Settings

## Navigation

Major panels include `Menu`. Active flows include `Cancel`, `Continue`, and `Back` where the previous step can be restored safely. If Back is not available, ATLAS explains that the user can continue or cancel.

If the user has an unfinished flow, `/start` and `Menu` show a continuation panel. The user can continue, restart the same flow after confirmation, cancel, or return to the menu.

Ordinary navigation tries to edit the current panel. If editing fails, ATLAS sends a new panel.

## Flow Completion

After check-in, day plan, habits, evening reflection and reports, ATLAS shows useful next actions such as day plan, habits, evening, report or menu.

## Empty States

When data is missing, ATLAS explains the next best step:

- reports need check-ins, habits and evening reflections;
- day plans work better after check-in, but a minimal plan is available;
- if there is no active habit, ATLAS asks the user to choose one small realistic habit.

## Settings and Profile

Settings show language, onboarding state, primary loop, planning style and important loops. Profile shows primary loop, current focus, planning style and important loops.

The user can change language and restart onboarding. Restart onboarding requires confirmation. Check-ins, habits and reports are not deleted.

Telegram Bot Token and webhook secret are never shown in Telegram settings or profile.

## Question

`Question` currently works as structured guidance toward ATLAS flows. Free-form LLM Q&A is outside this release.
