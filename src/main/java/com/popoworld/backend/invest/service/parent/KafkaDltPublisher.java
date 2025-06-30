package com.popoworld.backend.invest.service.parent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaDltPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String key, String message, Exception error) {
        try {
            kafkaTemplate.send(topic, key, message);
            log.warn("📦 DLT 전송 완료 (topic: {}, key: {})", topic, key);
        } catch (Exception e) {
            log.error("❗ DLT 전송 실패 (topic: {})", topic, e);
        }
    }
}