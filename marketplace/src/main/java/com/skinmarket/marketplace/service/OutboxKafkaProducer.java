package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.entity.OutboxEvent;
import com.skinmarket.marketplace.enums.OutboxEventType;
import com.skinmarket.marketplace.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxKafkaProducer {
    private final static Integer OUTBOX_MAX_RETRIES = 3;
    private final static Integer OUTBOX_LIMIT = 1;
    private final static String TOPIC = "purchases";

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final static Logger LOGGER = LoggerFactory.getLogger(OutboxKafkaProducer.class);

    public OutboxKafkaProducer(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 60000)
    public void produce() {
        LOGGER.info("Started scheduled process : process outbox events");
        List<OutboxEvent> events = outboxRepository.findUnprocessedPurchaseEventsByType(
                OUTBOX_LIMIT,
                OUTBOX_MAX_RETRIES,
                OutboxEventType.MARKET_ITEM_PURCHASED);
        if (events.isEmpty()) {
            LOGGER.info("No marketplace purchase events found");
            return;
        }
        for (OutboxEvent event : events) {
            LOGGER.info("Processing outbox event : {}", event);
            kafkaTemplate.send(TOPIC, event.aggregateId().toString(), event.payload());
            LOGGER.info("Sent to kafka event : {}", event);
        }

    }

}
