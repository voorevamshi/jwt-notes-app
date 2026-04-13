-- Run this against your notesdb database to clear the failed migration record.
-- After this, restart the application — Flyway will re-run V1 cleanly.

DELETE FROM flyway_schema_history WHERE version = '1' AND success = 0;
