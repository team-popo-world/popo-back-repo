package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotStoryRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.response.ChatbotResponsePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotProcessor {

    private final RedisTemplate<String, String> redisTemplate;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void process(ChatKafkaPayload payload) throws JsonProcessingException {
        UUID userId = payload.getUserId();
        UUID requestId = payload.getRequestId();
        String redisKey = "scenario:temp:" + userId;
        String cached = redisTemplate.opsForValue().get(redisKey);

        if (cached == null) {
            log.warn("⛔ Redis에 캐시 없음 (userId: {})", userId);
            return;
        }

        ChatbotStoryRequestDTO cachedDto = objectMapper.readValue(cached, ChatbotStoryRequestDTO.class);
        ChatbotStoryRequestDTO apiRequest = ChatbotStoryRequestDTO.builder()
                .chapterId(cachedDto.getChapterId())
                .story(cachedDto.getStory())
                .editRequest(payload.getEditRequest())
                .build();

        webClient.post()
                .uri("http://43.203.175.69:8004/edit-scenario")
                .bodyValue(apiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        result -> {
                            redisTemplate.opsForValue().set(redisKey, result, Duration.ofMinutes(30));

                            ChatbotResponsePayload responsePayload = ChatbotResponsePayload.builder()
                                    .userId(userId)
                                    .requestId(requestId)
                                    .story(result)
                                    .build();

                            try {
                                String responseJson = objectMapper.writeValueAsString(responsePayload);
                                kafkaTemplate.send("chatbot.response", userId.toString(), responseJson);
                                log.info("✅ FastAPI 응답 저장 및 Kafka 전송 완료 (userId: {})", userId);
                            } catch (JsonProcessingException e) {
                                log.error("❗ Kafka 전송용 JSON 직렬화 실패 (userId: {})", userId, e);
                            }
                        },
                        error -> {
                            log.error("❗ FastAPI 호출 실패 (userId: {}): {}", userId, error.getMessage(), error);
                            try {
                                String failedPayload = objectMapper.writeValueAsString(payload);
                                kafkaTemplate.send("chatbot.request.DLT", userId.toString(), failedPayload);
                                log.warn("📦 FastAPI 실패 DLT 전송 완료 (userId: {})", userId);
                            } catch (Exception ex) {
                                log.error("❗ DLT 전송 실패 (FastAPI 에러 처리 중)", ex);
                            }
                        }
                );
    }
}