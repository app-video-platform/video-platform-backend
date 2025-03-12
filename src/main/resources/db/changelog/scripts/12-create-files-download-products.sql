CREATE TABLE files_download_products (
                       id UUID PRIMARY KEY,
                       file_name VARCHAR(255),
                       user_id UUID,
                       section_id UUID NOT NULL,
                       size BIGINT,
                       path VARCHAR(255),
                       file_type VARCHAR(50),
                       hash VARCHAR(255),
                       uploaded_at TIMESTAMP,
                       download_count INTEGER,
                       is_public BOOLEAN,
                       CONSTRAINT fk_section
                           FOREIGN KEY (section_id)
                               REFERENCES sections_download_products(id)
);
