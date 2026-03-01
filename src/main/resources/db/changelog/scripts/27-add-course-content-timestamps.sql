ALTER TABLE course_sections
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();

ALTER TABLE course_sections
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();

UPDATE course_sections
SET created_at = now()
WHERE created_at IS NULL;

UPDATE course_sections
SET updated_at = COALESCE(updated_at, created_at, now())
WHERE updated_at IS NULL;

ALTER TABLE course_sections
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE course_sections
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE course_lessons
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now();

UPDATE course_lessons
SET updated_at = COALESCE(updated_at, created_at, now())
WHERE updated_at IS NULL;

ALTER TABLE course_lessons
    ALTER COLUMN updated_at SET NOT NULL;
