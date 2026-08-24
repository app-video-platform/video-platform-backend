CREATE TABLE storefront_configs (
    id UUID PRIMARY KEY,
    creator_id UUID NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    appearance VARCHAR(16) NOT NULL DEFAULT 'DARK',
    accent_color VARCHAR(7) NOT NULL DEFAULT '#ffbd41',
    typography VARCHAR(16) NOT NULL DEFAULT 'MODERN',
    featured_product_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_storefront_appearance CHECK (appearance IN ('LIGHT', 'DARK')),
    CONSTRAINT ck_storefront_typography CHECK (typography IN ('MODERN', 'CLASSIC', 'FRIENDLY')),
    CONSTRAINT ck_storefront_accent CHECK (accent_color LIKE '#______')
);

CREATE TABLE storefront_product_order (
    storefront_config_id UUID NOT NULL REFERENCES storefront_configs(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (storefront_config_id, position),
    CONSTRAINT uk_storefront_product UNIQUE (storefront_config_id, product_id)
);

CREATE INDEX idx_storefront_order_product ON storefront_product_order(product_id);
