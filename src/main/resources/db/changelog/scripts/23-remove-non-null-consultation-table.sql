ALTER TABLE consultation_products
    ALTER COLUMN duration_minutes      DROP NOT NULL,
    ALTER COLUMN meeting_method        DROP NOT NULL,
    ALTER COLUMN buffer_before_minutes DROP NOT NULL,
    ALTER COLUMN buffer_after_minutes  DROP NOT NULL,
    ALTER COLUMN cancellation_policy   DROP NOT NULL;
