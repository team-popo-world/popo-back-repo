package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatbotRequestConsumer {

    private final ObjectMapper objectMapper;
    private final ChatbotProcessor chatbotProcessor;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "chatbot.request", groupId = "chatbot-request-group")
    public void onRequest(@Payload String messageJson) {
        // 1. JSON → 객체
        try {
            ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);
            chatbotProcessor.process(payload);
        } catch (Exception e) {
            log.error("❗ Kafka 메시지 처리 실패 - DLT 후보: {}", e.getMessage(), e);
            try {
                ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);
                // 실패한 원본 메시지를 DLT 토픽에 전송
                kafkaTemplate.send("chatbot.request.DLT", payload.getUserId().toString(), messageJson);
                log.warn("📦 DLT로 메시지 전송 완료");
            } catch (Exception dltEx) {
                log.error("❗ DLT 전송 실패: {}", dltEx.getMessage(), dltEx);
            }
        }
    }
}
