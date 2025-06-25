package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.quiz.child.dto.QuizRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizKafkaConsumer {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "quiz.request", groupId = "quiz-worker-group")
    public void onQuizRequest(@Payload String messageJson) throws JsonProcessingException {
            QuizRequestPayload request = objectMapper.readValue(messageJson, QuizRequestPayload.class);

            UUID userId = request.getUserId();
            String difficulty = request.getDifficulty();
            String topic = request.getTopic();

            // 3. FastAPI 호출
            webClient.post()
                    .uri("http://15.164.94.158:8001/quiz/{difficulty}/{topic}", difficulty, topic)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(response -> {
                        String redisKey = "quiz:" + userId;
                        // 성공 시 Redis 저장
                        redisTemplate.opsForValue().set(redisKey, response, Duration.ofMinutes(5));
                        log.info("[퀴즈 생성 완료] Redis 저장됨: {}", redisKey);

                        // Kafka 응답 전송
                        kafkaTemplate.send("quiz.response", userId.toString(), "updated");

                    }, error -> {
                        // 예외 처리
                        log.error("[퀴즈 생성 실패] FastAPI 호출 에러", error);
                    });
    }
}
