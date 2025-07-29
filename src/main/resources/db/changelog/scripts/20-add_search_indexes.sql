-- 1) enable pg_trgm extension (once per database)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2) trigram indexes on each product table
CREATE INDEX IF NOT EXISTS idx_course_products_name_trgm
    ON course_products USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_download_products_name_trgm
    ON download_products USING GIN (name gin_trgm_ops);
-- repeat for your 3rd product table...

-- 3) trigram indexes on users
CREATE INDEX IF NOT EXISTS idx_users_first_name_trgm
    ON users USING GIN (first_name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_last_name_trgm
    ON users USING GIN (last_name gin_trgm_ops);
