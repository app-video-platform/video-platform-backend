
CREATE TABLE IF NOT EXISTS course_sections (
                                               id UUID PRIMARY KEY,
                                               title TEXT NOT NULL,
                                               description VARCHAR(1000),
                                               position INTEGER NOT NULL DEFAULT 0,    -- order within the course
                                               course_product_id UUID NOT NULL,        -- which course this section belongs to
                                               CONSTRAINT fk_section_to_course
                                                   FOREIGN KEY(course_product_id)
                                                       REFERENCES course_products(id)
                                                       ON DELETE CASCADE
);

-- Optional: speed up lookups by course_product_id
CREATE INDEX IF NOT EXISTS idx_course_sections_course_id
    ON course_sections(course_product_id);
