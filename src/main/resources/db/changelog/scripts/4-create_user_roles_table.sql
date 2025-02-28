CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL
);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_users
        FOREIGN KEY (user_id)
            REFERENCES users(user_id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_roles
        FOREIGN KEY (role_id)
            REFERENCES roles(role_id);
