
CREATE TABLE IF NOT EXISTS course_lessons (
                                              id UUID PRIMARY KEY,
                                              title TEXT NOT NULL,
                                              type TEXT NOT NULL,                     -- e.g. 'VIDEO' or 'ARTICLE' (application‐enforced)
                                              video_url TEXT,                         -- only non‐NULL if type = 'VIDEO'
                                              content TEXT,                           -- rich‐text JSON (when type = 'ARTICLE'), stored as plain TEXT
                                              position INTEGER NOT NULL DEFAULT 0,    -- order within the section
                                              created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                                              section_id UUID NOT NULL,               -- which section this lesson belongs to
                                              CONSTRAINT fk_lesson_to_section
                                                  FOREIGN KEY(section_id)
                                                      REFERENCES course_sections(id)
                                                      ON DELETE CASCADE
);

-- Optional: speed up lookups by section_id
CREATE INDEX IF NOT EXISTS idx_course_lessons_section_id
    ON course_lessons(section_id);
