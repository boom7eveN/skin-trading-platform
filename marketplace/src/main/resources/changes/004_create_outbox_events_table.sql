CREATE TABLE outbox_events
(
    id           UUID PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(100) NOT NULL,
    payload      JSONB        NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    processed    BOOLEAN      NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP,
    retry_count  INTEGER      NOT NULL DEFAULT 0,
    error        TEXT
);