# Privacy Controls

ATLAS exposes foundations for:

- `/privacy`: explain stored profile, check-ins, habits, reflections, reports, memory and Telegram identifiers.
- `/memory`: show memory categories and counts without raw sensitive content by default.
- `/export`: produce user-scoped JSON and Markdown export data.
- `/forget`: archive memory records only, after confirmation.
- `/delete_my_data`: delete user-scoped data after strong confirmation.

Destructive operations require the exact confirmation value `DELETE` in the service layer. Export, forget and deletion use the internal user id and do not operate across users.
