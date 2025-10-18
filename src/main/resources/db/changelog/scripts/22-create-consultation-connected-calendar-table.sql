CREATE TABLE IF NOT EXISTS consultation_connected_calendars
(
    id                UUID PRIMARY KEY,
    teacher_id        UUID                        NOT NULL, -- FK to users(id)
    provider          VARCHAR(32)                 NOT NULL,
    oauth_token_enc   TEXT                        NOT NULL,
    refresh_token_enc TEXT                        NOT NULL,
    expires_at        TIMESTAMP WITHOUT TIME ZONE NULL,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE consultation_connected_calendars
    ADD CONSTRAINT fk_calendar_teacher
        FOREIGN KEY (teacher_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_calendar_teacher
    ON consultation_connected_calendars (teacher_id);
