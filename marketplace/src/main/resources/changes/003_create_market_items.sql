CREATE TABLE market_items
(
    id         UUID PRIMARY KEY,
    seller_id  UUID           NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    skin_id    UUID           NOT NULL REFERENCES skins (id) ON DELETE CASCADE,
    price      DECIMAL(15, 2) NOT NULL,
    status     VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    sold_at    TIMESTAMP,
    version    BIGINT         NOT NULL DEFAULT 0
);