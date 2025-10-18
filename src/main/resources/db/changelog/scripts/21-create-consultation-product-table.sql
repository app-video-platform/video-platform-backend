CREATE TABLE IF NOT EXISTS consultation_products
(
    -- common product columns (copy these from your existing CourseProduct/DownloadProduct DDL)
    id                    UUID PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    description           TEXT         NULL,
    image                 VARCHAR(512) NULL,
    type                  VARCHAR(50)  NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    user_id               UUID         NOT NULL, -- FK to users(id)
    price                 BIGINT       NOT NULL,
    customers             INTEGER,
    created_at            TIMESTAMP,
    updated_at            TIMESTAMP,

    -- consultation‐specific columns
    duration_minutes      INT          NOT NULL,
    meeting_method        VARCHAR(32)  NOT NULL,
    custom_location       VARCHAR(255) NULL,
    buffer_before_minutes INT          NOT NULL DEFAULT 0,
    buffer_after_minutes  INT          NOT NULL DEFAULT 0,
    max_sessions_per_day  INT          NULL,
    confirmation_message  TEXT         NULL,
    cancellation_policy   TEXT         NOT NULL
);

ALTER TABLE consultation_products
    ADD CONSTRAINT fk_consultation_user
        FOREIGN KEY (user_id)
            REFERENCES users (user_id)
            ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_consultation_user
    ON consultation_products (user_id);
