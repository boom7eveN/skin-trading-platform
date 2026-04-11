package com.skinmarket.notification.service;

import com.skinmarket.notification.dto.MarketItemPurchasedEvent;
import com.skinmarket.notification.entity.ProcessedMessage;
import com.skinmarket.notification.enums.EventType;
import com.skinmarket.notification.repository.ProcessedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void consume(MarketItemPurchasedEvent event) {

        Optional<ProcessedMessage> alreadyExistsMessage = processedMessageRepository.
                findProcessedMessageByAggregateIdAndEventType(event.marketItemId(), EventType.MARKET_ITEM_PURCHASED);

        if (alreadyExistsMessage.isPresent()) {
            LOGGER.warn("УВЕДОМЛЕНИЕ ПРОДАВЦУ {} ОБ УСПЕШНОЙ ПРОДАЖЕ {} ЗА {} УЖЕ БЫЛО УСПЕШНО ОТПРАВЛЕНО РАНЕЕ, " +
                            "ПОЭТОМУ СНОВА НЕ ОТПРАВЛЯЕМ",
                    event.buyerId(), event.marketItemId(), event.price());
            return;
        }

        LOGGER.info("УВЕДОМЛЕНИЕ ПРОДАВЦУ {} ОБ УСПЕШНОЙ ПРОДАЖЕ {} ЗА {} ОТПРАВЛЕНО",
                event.buyerId(), event.marketItemId(), event.price());

        ProcessedMessage processedMessage = new ProcessedMessage(
                UUID.randomUUID(),
                event.marketItemId(),
                EventType.MARKET_ITEM_PURCHASED,
                LocalDateTime.now()
        );

        if (!processedMessageRepository.createProcessedMessage(processedMessage)) {
            LOGGER.error("ОБРАБОТАННОЕ СОБЫТИЕ {} НЕ УДАЛОСЬ СОХРАНИТЬ В БД", event);
        }
    }
}
