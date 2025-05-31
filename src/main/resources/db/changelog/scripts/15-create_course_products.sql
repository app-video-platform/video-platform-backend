CREATE TABLE IF NOT EXISTS course_products
(
    id          UUID PRIMARY KEY,
    name        TEXT                        NOT NULL,
    description VARCHAR(500),
    image       TEXT,
    type        TEXT                        NOT NULL,           -- should always be 'COURSE' via application logic
    status      TEXT                        NOT NULL,           -- e.g. 'DRAFT', 'PUBLISHED', etc.
    user_id     UUID                        NOT NULL,           -- references the owning user/teacher
    price       NUMERIC(10, 2)              NOT NULL DEFAULT 0,-- store “0” for free courses
    customers   INTEGER                     NOT NULL DEFAULT 0, -- number of customers enrolled/purchased
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_course_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_course_products_user_id ON course_products (user_id);
