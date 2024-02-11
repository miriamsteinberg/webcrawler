package com.example.consumer;

import com.example.webcrawler.WebPageReader;
import com.example.model.LinkData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private final WebPageReader webPageReader;
    private final ObjectMapper objectMapper;

    public KafkaConsumer(WebPageReader webPageReader, ObjectMapper objectMapper) {
        this.webPageReader = webPageReader;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "url-topic", groupId = "your-group-id")
    public void consumeMessage(String message) {
//       logger.info("Received message: " + message);
        try {
            LinkData obj = objectMapper.readValue(message, LinkData.class);
            webPageReader.findLinks(obj.getUrl(), obj.getDepth(), obj.getIndex());

        } catch (JsonProcessingException e) {
            logger.error("Error processing message: " + message, e);
        }
    }
}