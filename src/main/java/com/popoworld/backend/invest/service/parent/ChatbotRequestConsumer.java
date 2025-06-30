package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final KafkaDltPublisher kafkaDltPublisher;
    @KafkaListener(topics = "chatbot.request", groupId = "chatbot-request-group")
    public void onRequest(@Payload String messageJson) {
        long startTime = System.currentTimeMillis();
        log.info("❗ 시간시간시간시간시간시간시간시간시간시간시간시간시간시간", startTime);
        // 1. JSON → 객체
        try {
            ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);
            chatbotProcessor.process(payload);
        } catch (Exception e) {
            log.error("❗ Kafka 메시지 처리 실패 - DLT 후보: {}", e.getMessage(), e);
            try {
                ObjectNode payloadNode = (ObjectNode) objectMapper.readTree(messageJson);
                payloadNode.put("error", "processing_failure");

                String updatedJson = objectMapper.writeValueAsString(payloadNode);
                String userId = payloadNode.get("userId").asText();

                kafkaDltPublisher.send("chatbot.request.DLT", userId, updatedJson, e);
            } catch (Exception dltEx) {
                log.error("❗ DLT 전송 실패: {}", dltEx.getMessage(), dltEx);
            }
        }
    }
}
