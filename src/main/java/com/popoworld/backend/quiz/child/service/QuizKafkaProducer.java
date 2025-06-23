package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.quiz.child.dto.QuizRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendQuizRequest(String requestId, UUID userId, String difficulty, String topic) {
        try {
            QuizRequestPayload payload = new QuizRequestPayload(userId, difficulty, topic);
            String json = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send("quiz.request", requestId, json);
            log.info("[Kafka] 퀴즈 요청 전송 완료: {}", json);
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 직렬화 실패", e);
            throw new RuntimeException("Kafka 메시지 전송 실패", e);
        }
    }
}
