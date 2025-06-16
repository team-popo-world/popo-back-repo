package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotRequestConsumer {

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chatbot.request", groupId = "chatbot-worker-group")
    public void onRequest(@Payload String messageJson) throws JsonProcessingException {
        // 1. JSON → 객체
        ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);

        UUID userId = payload.getUserId();

        // 2. redis에 임시 저장되어있던 story 가져옴
        String redisKey = "scenario:temp:" + userId;
        String story = redisTemplate.opsForValue().get(redisKey);

        ChatbotStoryRequestDTO dto = objectMapper.readValue(story, ChatbotStoryRequestDTO.class);

        ChatbotStoryRequestDTO apiRequest = ChatbotStoryRequestDTO.builder()
                .chapterId(dto.getChapterId())
                .story(dto.getStory())
                .editRequest(payload.getEditRequest())
                .build();

        // 3. FastAPI 호출
        String response = webClient.post()
                .uri("http://15.164.94.158:8000/edit-scenario")
                .bodyValue(apiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // 4. Redis에 임시 저장 (30분 유지)
        redisTemplate.opsForValue().set(redisKey, response, Duration.ofMinutes(30));

        // 5. Kafka 응답 토픽에 메세지 보냄
        kafkaTemplate.send("chatbot.response", userId.toString(), "updated");
    }
}
