package com.skinmarket.notification.repository;

import com.skinmarket.notification.entity.ProcessedMessage;
import com.skinmarket.notification.enums.EventType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ProcessedMessageRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public ProcessedMessageRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<ProcessedMessage> findProcessedMessageByAggregateIdAndEventType(UUID aggregateId, EventType eventType) {
        String sql = """
                SELECT id, aggregate_id, event_type, processed_at
                FROM processed_messages
                WHERE aggregate_id = :aggregateId AND
                event_type = :eventType""";
        List<ProcessedMessage> messages = jdbc.query(sql,
                Map.of("aggregateId", aggregateId, "eventType", eventType.name()),
                PROCESSED_MESSAGE_ROW_MAPPER);
        return messages.stream().findFirst();
    }

    public boolean createProcessedMessage(ProcessedMessage processedMessage) {
        String sql = """
                        INSERT INTO processed_messages(id, aggregate_id, event_type, processed_at)
                        VALUES(:id, :aggregateId, :eventType, :processedAt)
                """;
        return jdbc.update(sql, Map.of(
                "id", processedMessage.id(),
                "aggregateId", processedMessage.aggregateId(),
                "eventType", processedMessage.eventType().toString(),
                "processedAt", processedMessage.processedAt())) == 1;
    }

    private static final RowMapper<ProcessedMessage> PROCESSED_MESSAGE_ROW_MAPPER = (rs, _) -> new ProcessedMessage(
            rs.getObject("id", UUID.class),
            rs.getObject("aggregate_id", UUID.class),
            EventType.valueOf(rs.getString("event_type")),
            rs.getTimestamp("processed_at").toLocalDateTime()
    );
}
