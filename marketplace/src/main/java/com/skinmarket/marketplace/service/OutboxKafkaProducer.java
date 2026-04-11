package com.skinmarket.marketplace.service;

import com.skinmarket.marketplace.entity.OutboxEvent;
import com.skinmarket.marketplace.enums.OutboxEventType;
import com.skinmarket.marketplace.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        List<OutboxEvent> outboxPurchaseEvents = outboxRepository.findUnprocessedOutboxEventsByType(
                OUTBOX_LIMIT,
                OUTBOX_MAX_RETRIES,
                OutboxEventType.MARKET_ITEM_PURCHASED);
        if (outboxPurchaseEvents.isEmpty()) {
            LOGGER.info("No marketplace purchase events found");
            return;
        }
        for (OutboxEvent outboxPurchaseEvent: outboxPurchaseEvents) {
            try {

                LOGGER.info("Processing outbox event : {}", outboxPurchaseEvent);

                kafkaTemplate.send(TOPIC, outboxPurchaseEvent.aggregateId().toString(), outboxPurchaseEvent.payload())
                .get(10, TimeUnit.SECONDS);

                outboxRepository.markAsProcessed(outboxPurchaseEvent.id(), LocalDateTime.now());

                LOGGER.info("Sent to kafka event : {}", outboxPurchaseEvent);

            } catch (Exception e) {

                LOGGER.error("Failed to send event to Kafka: {}", outboxPurchaseEvent.id(), e);

                int newRetryCount = outboxPurchaseEvent.retryCount() + 1;

                if (newRetryCount >= OUTBOX_MAX_RETRIES) {

                    outboxRepository.markAsDeadLetter(outboxPurchaseEvent.id(), e.getMessage());
                    LOGGER.warn("Event marked as dead letter: {}, error: {}", outboxPurchaseEvent.id(), e.getMessage());

                } else {

                    outboxRepository.incrementRetryCount(outboxPurchaseEvent.id(), e.getMessage());
                    LOGGER.warn("Event retry count increased: {}, retry: {}/{}",
                            outboxPurchaseEvent.id(), newRetryCount, OUTBOX_MAX_RETRIES);
                }
            }
        }

    }

}
