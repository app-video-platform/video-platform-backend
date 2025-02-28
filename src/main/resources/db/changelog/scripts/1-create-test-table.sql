CREATE TABLE dummy (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
