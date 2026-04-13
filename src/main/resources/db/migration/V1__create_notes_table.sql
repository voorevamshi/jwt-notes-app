-- V1__create_notes_table.sql
-- Safe migration: creates the notes table for the first time.
-- Uses MySQL-compatible types and avoids destructive operations.

CREATE TABLE IF NOT EXISTS notes (
    id             BIGINT          NOT NULL AUTO_INCREMENT,
    title          VARCHAR(255)    NOT NULL,
    content        TEXT            NOT NULL,
    owner_username VARCHAR(100)    NULL,
    created_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_notes PRIMARY KEY (id)
);

-- Index for fast title search (case-insensitive collation handled by MySQL default utf8mb4_general_ci)
CREATE INDEX idx_notes_title ON notes (title);

-- Index prepared for future user-ownership queries
CREATE INDEX idx_notes_owner ON notes (owner_username);
