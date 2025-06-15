package com.popoworld.backend.invest.service.parent;

import com.popoworld.backend.invest.service.SseEmitters;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotResponseConsumer {

    private final SseEmitters sseEmitters;
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "chatbot.response", groupId = "chatbot-response-group")
    public void onResponse(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String userId) {

        UUID userIds = UUID.fromString(userId);
        String redisKey = "scenario:temp:" + userId;
        String data = redisTemplate.opsForValue().get(redisKey);


        if (data != null) {
            // SSE로 해당 유저에게 푸시
            System.out.println("cocococococococo");
            sseEmitters.send(userIds, data);
            System.out.println("bbbbbbbbbbbbbbbb");
        }
    }
}
