--liquibase formatted sql
--changeset video-platform:40-create-product-media
CREATE TABLE product_media (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    kind VARCHAR(24) NOT NULL,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    cdn_url VARCHAR(2048) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    gallery_position INTEGER NOT NULL,
    status VARCHAR(24) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX idx_product_media_product_kind ON product_media(product_id, kind);
CREATE UNIQUE INDEX uq_product_media_kind_position ON product_media(product_id, kind, gallery_position);
