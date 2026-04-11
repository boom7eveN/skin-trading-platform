package com.skinmarket.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OutboxKafkaConsumer {
    private final static Logger LOGGER= LoggerFactory.getLogger(OutboxKafkaConsumer.class);
    @KafkaListener(topics = "purchases")
    public void consume(String payload) {
        LOGGER.info(payload);
    }
}
