CREATE TABLE commerce_orders (
    id UUID PRIMARY KEY,
    buyer_user_id UUID NOT NULL REFERENCES users(user_id),
    creator_user_id UUID NOT NULL REFERENCES users(user_id),
    status VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    subtotal_minor BIGINT NOT NULL,
    total_minor BIGINT NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    checkout_fingerprint VARCHAR(64) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    refunded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_commerce_orders_buyer_idempotency UNIQUE (buyer_user_id, idempotency_key),
    CONSTRAINT ck_commerce_orders_amounts CHECK (subtotal_minor > 0 AND total_minor > 0)
);

CREATE INDEX idx_commerce_orders_buyer_created
    ON commerce_orders(buyer_user_id, created_at DESC);
CREATE INDEX idx_commerce_orders_creator_created
    ON commerce_orders(creator_user_id, created_at DESC);
CREATE INDEX idx_commerce_orders_status_expiry
    ON commerce_orders(status, expires_at);

CREATE TABLE commerce_order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES commerce_orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    product_type VARCHAR(40) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    unit_amount_minor BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    line_total_minor BIGINT NOT NULL,
    CONSTRAINT uk_commerce_order_items_order_product UNIQUE (order_id, product_id),
    CONSTRAINT ck_commerce_order_items_amounts CHECK (
        unit_amount_minor > 0 AND quantity = 1 AND line_total_minor = unit_amount_minor
    )
);

CREATE INDEX idx_commerce_order_items_product
    ON commerce_order_items(product_id);

CREATE TABLE commerce_payment_attempts (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE REFERENCES commerce_orders(id) ON DELETE CASCADE,
    provider VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    provider_session_id VARCHAR(255) NOT NULL UNIQUE,
    provider_payment_id VARCHAR(255) UNIQUE,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    checkout_url VARCHAR(2048),
    failure_code VARCHAR(100),
    failure_message VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_commerce_payment_attempt_amount CHECK (amount_minor > 0)
);

CREATE TABLE commerce_payment_events (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    provider_event_id VARCHAR(255) NOT NULL,
    order_id UUID NOT NULL REFERENCES commerce_orders(id),
    event_type VARCHAR(32) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_commerce_payment_events_provider_event UNIQUE (provider, provider_event_id)
);

CREATE INDEX idx_commerce_payment_events_order
    ON commerce_payment_events(order_id);

ALTER TABLE product_entitlements
    ADD COLUMN purchase_order_item_id UUID;

ALTER TABLE product_entitlements
    ADD CONSTRAINT fk_product_entitlements_purchase_order_item
        FOREIGN KEY (purchase_order_item_id) REFERENCES commerce_order_items(id);

CREATE UNIQUE INDEX uk_product_entitlements_purchase_order_item
    ON product_entitlements(purchase_order_item_id)
    WHERE purchase_order_item_id IS NOT NULL;
