CREATE EXTENSION IF NOT EXISTS "pgcrypto";

ALTER TABLE user_roles DROP CONSTRAINT fk_user_roles_users;

ALTER TABLE verification_tokens DROP CONSTRAINT fk_verification_tokens_users;

ALTER TABLE users
    ALTER COLUMN user_id DROP DEFAULT;

ALTER TABLE users
    ALTER COLUMN user_id TYPE uuid
        USING gen_random_uuid();

ALTER TABLE users
    ALTER COLUMN user_id SET DEFAULT gen_random_uuid();

ALTER TABLE user_roles
    ALTER COLUMN user_id TYPE uuid
        USING (NULL::uuid);

ALTER TABLE verification_tokens
    ALTER COLUMN user_id TYPE uuid
        USING (NULL::uuid);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE verification_tokens
    ADD CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id) REFERENCES users(user_id);
