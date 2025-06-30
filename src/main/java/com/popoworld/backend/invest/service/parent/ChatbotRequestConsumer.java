package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRequestConsumer {

    private final ObjectMapper objectMapper;
    private final ChatbotProcessor chatbotProcessor;

    @KafkaListener(topics = "chatbot.request", groupId = "chatbot-request-group")
    public void onRequest(@Payload String messageJson) {
        // 1. JSON → 객체
        try {
            ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);
            chatbotProcessor.process(payload);
        } catch (Exception e) {
            log.error("❗ Kafka 메시지 처리 실패 - DLT 후보: {}", e.getMessage(), e);
            // TODO: kafkaTemplate.send("chatbot.request.DLT", messageJson); 도입 가능
        }
    }
}
