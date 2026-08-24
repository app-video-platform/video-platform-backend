--liquibase formatted sql

--changeset video-platform:34-add-creator-reporting-indexes
CREATE INDEX idx_commerce_orders_creator_paid_at
    ON commerce_orders (creator_user_id, paid_at);

CREATE INDEX idx_commerce_orders_creator_failed_at
    ON commerce_orders (creator_user_id, failed_at);

CREATE INDEX idx_commerce_orders_creator_refunded_at
    ON commerce_orders (creator_user_id, refunded_at);

CREATE INDEX idx_commerce_orders_creator_buyer_status
    ON commerce_orders (creator_user_id, buyer_user_id, status);

CREATE INDEX idx_product_entitlements_product_user_status
    ON product_entitlements (product_id, user_id, status);
