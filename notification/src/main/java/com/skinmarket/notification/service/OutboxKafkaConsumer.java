package com.skinmarket.notification.service;

import com.skinmarket.notification.dto.MarketItemPurchasedEvent;
import com.skinmarket.notification.entity.ProcessedMessage;
import com.skinmarket.notification.enums.EventType;
import com.skinmarket.notification.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxKafkaConsumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(OutboxKafkaConsumer.class);
    private final ProcessedMessageRepository processedMessageRepository;

    public OutboxKafkaConsumer(ProcessedMessageRepository processedMessageRepository) {
        this.processedMessageRepository = processedMessageRepository;
    }

    @KafkaListener(topics = "purchases")
    public void consume(MarketItemPurchasedEvent event, Acknowledgment ack) {
        try {
            Optional<ProcessedMessage> alreadyExistsMessage = processedMessageRepository.
                    findProcessedMessageByAggregateIdAndEventType(event.marketItemId(), EventType.MARKET_ITEM_PURCHASED);

            if (alreadyExistsMessage.isPresent()) {
                LOGGER.warn("NOTIFICATION TO SELLER {} OF THE SUCCESSFUL SALE OF {} FOR {} HAS ALREADY BEEN " +
                                "SENT PREVIOUSLY, SO WE WILL NOT SEND IT AGAIN",
                        event.buyerId(), event.marketItemId(), event.price());
                ack.acknowledge();
                return;
            }

            LOGGER.info("NOTIFICATION TO SELLER {} OF SUCCESSFUL SALE OF {} FOR {} SENT",
                    event.buyerId(), event.marketItemId(), event.price());

            ProcessedMessage processedMessage = new ProcessedMessage(
                    UUID.randomUUID(),
                    event.marketItemId(),
                    EventType.MARKET_ITEM_PURCHASED,
                    LocalDateTime.now()
            );

            if (!processedMessageRepository.createProcessedMessage(processedMessage)) {
                LOGGER.error("PROCESSED EVENT {} FAILED TO BE SAVED TO THE DB", event);
                return;
            }
            ack.acknowledge();
        } catch (Exception e) {
            LOGGER.error("UNEXPECTED ERROR WHILE PROCESSING {}", event, e);
        }

    }
}
