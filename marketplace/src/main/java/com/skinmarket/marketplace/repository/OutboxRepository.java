package com.skinmarket.marketplace.repository;

import com.skinmarket.marketplace.entity.OutboxEvent;
import com.skinmarket.marketplace.enums.OutboxEventType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class OutboxRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OutboxRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean createOutboxEvent(OutboxEvent event) {
        String sql = """
                INSERT INTO outbox_events(id, aggregate_id, event_type, payload, created_at, processed, retry_count)
                VALUES (:id, :aggregateId, :eventType, :payload::jsonb, :createdAt, :processed, :retryCount)
                """;

        return jdbc.update(sql, Map.of(
                "id", event.id(),
                "aggregateId", event.aggregateId(),
                "eventType", event.eventType().name(),
                "payload", event.payload(),
                "createdAt", event.createdAt(),
                "processed", event.processed(),
                "retryCount", event.retryCount()
        )) == 1;
    }

    public List<OutboxEvent> findUnprocessedEvents(Integer limit, Integer maxRetries) {
        String sql = """
                SELECT id, aggregate_id, event_type, payload, created_at, processed, processed_at, retry_count, error
                FROM outbox_events
                WHERE processed = false AND retry_count < :maxRetries
                ORDER BY created_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """;

        return jdbc.query(sql, Map.of("limit", limit, "maxRetries", maxRetries), OUTBOX_EVENT_MAPPER);
    }

    private static final RowMapper<OutboxEvent> OUTBOX_EVENT_MAPPER = (rs, _) -> new OutboxEvent(
            rs.getObject("id", UUID.class),
            rs.getObject("aggregate_id", UUID.class),
            OutboxEventType.valueOf(rs.getString("event_type")),
            rs.getString("payload"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBoolean("processed"),
            rs.getTimestamp("processed_at") != null
                    ? rs.getTimestamp("processed_at").toLocalDateTime()
                    : null,
            rs.getInt("retry_count"),
            rs.getString("error")
    );
}