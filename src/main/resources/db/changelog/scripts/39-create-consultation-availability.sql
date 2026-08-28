--liquibase formatted sql
--changeset video-platform:39-create-consultation-availability
CREATE TABLE consultation_availability_days (
    id UUID PRIMARY KEY,
    consultation_product_id UUID NOT NULL REFERENCES consultation_products(id) ON DELETE CASCADE,
    weekday VARCHAR(16) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_consultation_availability_day UNIQUE (consultation_product_id, weekday)
);

CREATE TABLE consultation_availability_windows (
    id UUID PRIMARY KEY,
    availability_day_id UUID NOT NULL REFERENCES consultation_availability_days(id) ON DELETE CASCADE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    position INTEGER NOT NULL
);

CREATE INDEX idx_consultation_availability_product ON consultation_availability_days(consultation_product_id);
CREATE INDEX idx_consultation_availability_window_day ON consultation_availability_windows(availability_day_id, position);
