CREATE TABLE skins
(
    id          UUID PRIMARY KEY,
    name        VARCHAR(200)   NOT NULL,
    weapon_type VARCHAR(100)   NOT NULL,
    rarity      VARCHAR(50)    NOT NULL,
    base_price  DECIMAL(15, 2) NOT NULL,
    version     BIGINT         NOT NULL DEFAULT 0
);