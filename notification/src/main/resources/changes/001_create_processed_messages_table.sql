CREATE TABLE processed_messages
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID         NOT NULL,--marketitemid
    event_type   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_aggregate_event UNIQUE (aggregate_id, event_type)
);