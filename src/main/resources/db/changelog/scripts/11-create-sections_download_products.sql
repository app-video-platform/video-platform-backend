CREATE TABLE sections_download_products (
                          id UUID PRIMARY KEY,
                          title VARCHAR(255),
                          description VARCHAR(1000),
                          position INTEGER,
                          download_product_id UUID NOT NULL,
                          CONSTRAINT fk_download_product
                              FOREIGN KEY (download_product_id)
                                  REFERENCES download_products(id)
);
