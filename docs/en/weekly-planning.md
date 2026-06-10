# Weekly Planning

Weekly planning adds a persisted weekly focus per user and week. The week starts on Monday.

The Telegram command `/week <main focus>` saves or updates the current weekly focus. `/week` without an argument shows the current focus.

Weekly reports include the saved weekly focus when available. The model is intentionally small: it stores the focus text and week start without introducing external calendar sync.
