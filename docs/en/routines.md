# Routines

Routine preferences store:

- daily check-in time;
- evening reflection time;
- timezone;
- quiet hours;
- enabled flag.

The reminder scheduler checks whether a reminder may be sent without interrupting quiet hours and claims a per-user, per-day reminder key to prevent duplicate check-in or evening reminders.

The Telegram settings panel links to the routines panel so users can see the current persisted reminder preferences.
