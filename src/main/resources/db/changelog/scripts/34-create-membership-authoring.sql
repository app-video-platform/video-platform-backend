ALTER TABLE course_products
    ADD COLUMN pricing_model VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME';
ALTER TABLE course_products
    ADD COLUMN billing_interval VARCHAR(20);
ALTER TABLE course_products
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE course_products
    ADD CONSTRAINT ck_course_product_pricing CHECK (
        currency = 'EUR'
        AND ((pricing_model = 'ONE_TIME' AND billing_interval IS NULL)
            OR (pricing_model = 'RECURRING' AND billing_interval IN ('MONTH', 'YEAR')))
    );

ALTER TABLE download_products
    ADD COLUMN pricing_model VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME';
ALTER TABLE download_products
    ADD COLUMN billing_interval VARCHAR(20);
ALTER TABLE download_products
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE download_products
    ADD CONSTRAINT ck_download_product_pricing CHECK (
        currency = 'EUR'
        AND ((pricing_model = 'ONE_TIME' AND billing_interval IS NULL)
            OR (pricing_model = 'RECURRING' AND billing_interval IN ('MONTH', 'YEAR')))
    );

ALTER TABLE consultation_products
    ADD COLUMN pricing_model VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME';
ALTER TABLE consultation_products
    ADD COLUMN billing_interval VARCHAR(20);
ALTER TABLE consultation_products
    ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'EUR';
ALTER TABLE consultation_products
    ADD CONSTRAINT ck_consultation_product_pricing CHECK (
        currency = 'EUR'
        AND ((pricing_model = 'ONE_TIME' AND billing_interval IS NULL)
            OR (pricing_model = 'RECURRING' AND billing_interval IN ('MONTH', 'YEAR')))
    );

CREATE TABLE membership_products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(420),
    image VARCHAR(512),
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    price NUMERIC(19, 2) NOT NULL DEFAULT 0,
    pricing_model VARCHAR(20) NOT NULL DEFAULT 'RECURRING',
    billing_interval VARCHAR(20) NOT NULL DEFAULT 'MONTH',
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    customers INTEGER NOT NULL DEFAULT 0,
    ordering_mode VARCHAR(30) NOT NULL DEFAULT 'NEWEST_FIRST',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_membership_product_type CHECK (type = 'MEMBERSHIP'),
    CONSTRAINT ck_membership_product_status CHECK (status IN ('DRAFT', 'HIDDEN')),
    CONSTRAINT ck_membership_product_price CHECK (price >= 0),
    CONSTRAINT ck_membership_product_pricing CHECK (
        pricing_model = 'RECURRING'
        AND billing_interval IN ('MONTH', 'YEAR')
        AND currency = 'EUR'
    )
);

CREATE INDEX idx_membership_products_user_id ON membership_products(user_id);

CREATE TABLE membership_content (
    id UUID PRIMARY KEY,
    membership_product_id UUID NOT NULL REFERENCES membership_products(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    body TEXT,
    asset_file_id UUID,
    asset_file_name VARCHAR(255),
    asset_file_type VARCHAR(150),
    asset_size BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_membership_content_type CHECK (type IN ('POST', 'VIDEO', 'RESOURCE')),
    CONSTRAINT ck_membership_content_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    CONSTRAINT ck_membership_content_shape CHECK (
        (type = 'POST' AND body IS NOT NULL AND asset_file_id IS NULL
            AND asset_file_name IS NULL AND asset_file_type IS NULL AND asset_size IS NULL)
        OR
        (type IN ('VIDEO', 'RESOURCE') AND body IS NULL AND asset_file_id IS NOT NULL
            AND asset_file_name IS NOT NULL AND asset_file_type IS NOT NULL AND asset_size > 0)
    )
);

CREATE INDEX idx_membership_content_membership ON membership_content(membership_product_id);

CREATE TABLE membership_feed_entries (
    id UUID PRIMARY KEY,
    membership_product_id UUID NOT NULL REFERENCES membership_products(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL,
    content_id UUID REFERENCES membership_content(id) ON DELETE CASCADE,
    included_product_id UUID,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    position INTEGER,
    CONSTRAINT uk_membership_feed_content UNIQUE (membership_product_id, content_id),
    CONSTRAINT uk_membership_feed_product UNIQUE (membership_product_id, included_product_id),
    CONSTRAINT ck_membership_feed_kind CHECK (kind IN ('CONTENT', 'PRODUCT')),
    CONSTRAINT ck_membership_feed_shape CHECK (
        (kind = 'CONTENT' AND content_id IS NOT NULL AND included_product_id IS NULL)
        OR
        (kind = 'PRODUCT' AND content_id IS NULL AND included_product_id IS NOT NULL)
    ),
    CONSTRAINT ck_membership_feed_position CHECK (position IS NULL OR position > 0)
);

CREATE INDEX idx_membership_feed_order
    ON membership_feed_entries(membership_product_id, position, added_at);
CREATE INDEX idx_membership_feed_included_product
    ON membership_feed_entries(included_product_id);
