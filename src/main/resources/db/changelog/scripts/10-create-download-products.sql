CREATE TABLE download_products (
                                   id UUID PRIMARY KEY,
                                   name VARCHAR(255),
                                   description VARCHAR(420),
                                   image VARCHAR(255),
                                   type VARCHAR(50),
                                   status VARCHAR(50),
                                   user_id UUID NOT NULL,
                                   price DECIMAL(19,2),
                                   customers INTEGER,
                                   created_at TIMESTAMP,
                                   updated_at TIMESTAMP
);
