CREATE TABLE IF NOT EXISTS product_entitlements (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_type VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    source VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_product_entitlements_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_entitlements_product
    ON product_entitlements(product_id);

CREATE INDEX IF NOT EXISTS idx_product_entitlements_user_status
    ON product_entitlements(user_id, status);
