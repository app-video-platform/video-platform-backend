CREATE TABLE product_landing_page_configs (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE,
    marketing_description VARCHAR(1200),
    hero_layout VARCHAR(20) NOT NULL DEFAULT 'MEDIA_RIGHT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_landing_hero_layout CHECK (hero_layout IN ('MEDIA_RIGHT', 'MEDIA_LEFT'))
);

CREATE TABLE product_landing_visible_sections (
    config_id UUID NOT NULL REFERENCES product_landing_page_configs(id) ON DELETE CASCADE,
    section_id VARCHAR(16) NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (config_id, position),
    CONSTRAINT uk_landing_visible_section UNIQUE (config_id, section_id),
    CONSTRAINT ck_landing_visible_section CHECK (section_id IN ('ABOUT', 'CONTENTS', 'CREATOR'))
);

CREATE TABLE product_landing_section_order (
    config_id UUID NOT NULL REFERENCES product_landing_page_configs(id) ON DELETE CASCADE,
    section_id VARCHAR(16) NOT NULL,
    position INTEGER NOT NULL,
    PRIMARY KEY (config_id, position),
    CONSTRAINT uk_landing_order_section UNIQUE (config_id, section_id),
    CONSTRAINT ck_landing_order_section CHECK (section_id IN ('ABOUT', 'CONTENTS', 'CREATOR'))
);

CREATE INDEX idx_landing_config_product ON product_landing_page_configs(product_id);
