# Privacy Controls

ATLAS exposes user-facing controls for:

- `/privacy`: explain stored profile, check-ins, habits, reflections, reports, memory and Telegram identifiers.
- `/memory`: show memory categories and counts without raw sensitive content by default.
- `/export`: produce user-scoped JSON and Markdown export data with saved check-ins, habits, reflections and memory records.
- `/forget DELETE`: archive memory records only, after confirmation.
- `/delete_my_data DELETE`: delete user-scoped profile, tracking, conversation, message, routine, planning, report, integration and memory data after strong confirmation.

Destructive operations require the exact confirmation value `DELETE` in the service layer. Export, forget and deletion use the internal user id and do not operate across users.
