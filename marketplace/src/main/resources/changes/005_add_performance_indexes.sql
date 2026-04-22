--пагин скинов
CREATE INDEX IF NOT EXISTS idx_skins_name
    ON skins (name);

--пагинация лотов
CREATE INDEX IF NOT EXISTS idx_market_items_created_at_desc
    ON market_items (created_at DESC);

--select фоновым процессом для отправки в кафку
CREATE INDEX IF NOT EXISTS idx_outbox_events_dispatch_queue
    ON outbox_events (event_type, retry_count, created_at)
    WHERE processed = FALSE AND is_dead_letter = FALSE;
