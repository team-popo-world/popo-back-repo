package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.response.ChatbotResponsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotResponseConsumer {

    private final ChatbotSseEmitters sseEmitters;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chatbot.response", groupId = "chatbot-response-group")
    public void onResponse(@Payload String message) {
        try {
            ChatbotResponsePayload payload = objectMapper.readValue(message, ChatbotResponsePayload.class);
            UUID userId = payload.getUserId();
            String story = payload.getStory();

            // 바로 story 사용 (Redis 조회 X)
            sseEmitters.send(userId, story);

            log.info("📤 SSE 응답 전송 완료 (userId: {}, requestId: {})", userId, payload.getRequestId());

        } catch (Exception e) {
            log.error("❗ Kafka 응답 처리 실패", e);
        }
    }
}
