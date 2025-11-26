CREATE TABLE IF NOT EXISTS course_quizzes (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL UNIQUE,
    title TEXT NOT NULL,
    description TEXT,
    passing_score INTEGER CHECK (passing_score BETWEEN 0 AND 100),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_quiz_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS quiz_questions (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    title TEXT NOT NULL,
    type TEXT NOT NULL,
    points INTEGER NOT NULL CHECK (points > 0),
    explanation TEXT,
    sort_order INTEGER,
    CONSTRAINT fk_question_quiz FOREIGN KEY (quiz_id) REFERENCES course_quizzes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_quiz_questions_quiz_id ON quiz_questions(quiz_id);

CREATE TABLE IF NOT EXISTS quiz_options (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL,
    text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER,
    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES quiz_questions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_quiz_options_question_id ON quiz_options(question_id);

CREATE TABLE IF NOT EXISTS quiz_attempts (
    id UUID PRIMARY KEY,
    quiz_id UUID NOT NULL,
    lesson_id UUID NOT NULL,
    user_id UUID NOT NULL,
    submitted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    points_possible INTEGER NOT NULL,
    points_achieved INTEGER NOT NULL,
    percentage NUMERIC(5,1) NOT NULL,
    passed BOOLEAN NOT NULL,
    answers_json TEXT NOT NULL,
    CONSTRAINT fk_attempt_quiz FOREIGN KEY (quiz_id) REFERENCES course_quizzes(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_lesson FOREIGN KEY (lesson_id) REFERENCES course_lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_attempt_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_quiz_attempts_quiz_user ON quiz_attempts(quiz_id, user_id);
CREATE INDEX IF NOT EXISTS idx_quiz_attempts_lesson ON quiz_attempts(lesson_id);
