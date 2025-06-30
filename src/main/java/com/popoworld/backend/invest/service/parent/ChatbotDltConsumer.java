package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotDltConsumer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Set<String> RETRYABLE_ERRORS = Set.of(
            "fastapi_failure", "processing_failure", "temporary_issue", "redis_miss"
    );

    @KafkaListener(topics = "chatbot.request.DLT", groupId = "chatbot-dlt-retry-group")
    public void onDltMessage(@Payload String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String errorType = root.has("error") ? root.get("error").asText() : "unknown";
            String userId = root.has("userId") ? root.get("userId").asText() : null;

            if ("serialization_failure".equals(errorType)) {
                log.warn("⛔ 직렬화 실패 메시지는 재처리하지 않음 (userId: {})", userId);
                return;
            }

            if (RETRYABLE_ERRORS.contains(errorType)) {
                kafkaTemplate.send("chatbot.request", userId, message);
                log.info("🔁 DLT 메시지 재전송 완료 (userId: {}, errorType: {})", userId, errorType);
            } else {
                log.warn("❓ 알 수 없는 에러 타입 - 수동 확인 필요 (userId: {}, errorType: {})", userId, errorType);
            }

        } catch (Exception e) {
            log.error("❗ DLT 메시지 역직렬화 또는 재처리 실패", e);
        }
    }
}