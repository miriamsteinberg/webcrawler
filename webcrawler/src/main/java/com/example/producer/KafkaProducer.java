package com.example.producer;

import com.example.model.LinkData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    private final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, LinkData> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, LinkData> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(LinkData message) {
        kafkaTemplate.send("url-topic", message);
    }
}