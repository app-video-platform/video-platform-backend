CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id BIGSERIAL PRIMARY KEY,
                                              token VARCHAR(255) NOT NULL UNIQUE,
                                              user_email VARCHAR(255) NOT NULL,
                                              expiry_date TIMESTAMP NOT NULL
);
