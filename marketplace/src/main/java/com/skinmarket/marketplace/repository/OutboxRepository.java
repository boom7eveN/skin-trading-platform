package com.skinmarket.marketplace.repository;

import com.skinmarket.marketplace.entity.OutboxEvent;
import com.skinmarket.marketplace.enums.OutboxEventType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    public List<OutboxEvent> findUnprocessedOutboxEventsByType(
            Integer limit, Integer maxRetries, OutboxEventType eventType) {
        String sql = """
                SELECT id, aggregate_id, event_type, payload, created_at, processed, processed_at, retry_count,
                       last_error, is_dead_letter
                FROM outbox_events
                WHERE NOT processed AND retry_count < :maxRetries AND event_type = :eventType AND NOT is_dead_letter
                ORDER BY created_at
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """;

        return jdbc.query(sql, Map.of(
                "limit", limit,
                "maxRetries", maxRetries,
                "eventType", eventType.name()), OUTBOX_EVENT_MAPPER);
    }

    public void markAsProcessed(UUID eventId, LocalDateTime processedAt) {
        String sql = """
                UPDATE outbox_events
                SET processed = true,
                    processed_at = :processedAt,
                    last_error = NULL
                WHERE id = :eventId
                """;

        jdbc.update(sql, Map.of("processedAt", processedAt, "eventId", eventId));
    }

    public void markAsDeadLetter(UUID eventId, String lastError) {
        String sql = """
            UPDATE outbox_events
            SET is_dead_letter = true,
                last_error = :lastError,
                retry_count = retry_count + 1
            WHERE id = :eventId
            """;

        jdbc.update(sql, Map.of("lastError", lastError, "eventId", eventId));
    }

    public void incrementRetryCount(UUID eventId, String lastError) {
        String sql = """
            UPDATE outbox_events
            SET retry_count = retry_count + 1,
                last_error = :lastError
            WHERE id = :eventId
            """;

        jdbc.update(sql, Map.of("lastError", lastError, "eventId", eventId));
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
            rs.getString("last_error"),
            rs.getBoolean("is_dead_letter")
    );
}