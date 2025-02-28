CREATE TABLE IF NOT EXISTS verification_tokens (
                                                   token_id BIGSERIAL PRIMARY KEY,
                                                   token VARCHAR(255) NOT NULL UNIQUE,
                                                   user_id BIGINT NOT NULL,
                                                   expiry_date TIMESTAMP NOT NULL
);

ALTER TABLE verification_tokens
    ADD CONSTRAINT fk_verification_tokens_users
        FOREIGN KEY (user_id)
            REFERENCES users(user_id);
