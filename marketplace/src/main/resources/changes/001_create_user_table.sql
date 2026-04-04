CREATE TABLE users
(
    id           UUID PRIMARY KEY,
    username     VARCHAR(100) UNIQUE NOT NULL,
    passwordHash VARCHAR(255)        NOT NULL,
    balance      DECIMAL(15, 2)      NOT NULL DEFAULT 0,
    role         VARCHAR(100)        NOT NULL DEFAULT 'USER',
    version      BIGINT              NOT NULL DEFAULT 0
);