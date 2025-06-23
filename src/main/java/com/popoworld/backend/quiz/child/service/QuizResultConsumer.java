package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.quiz.child.entity.QuizHistory;
import com.popoworld.backend.quiz.child.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizResultConsumer {

    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "quiz-history", groupId = "quiz-consumer-group")
    public void consume(@Payload String message) {
        try {
            QuizHistory history = objectMapper.readValue(message, QuizHistory.class);
            quizRepository.save(history);
            log.info("✅ Kafka 메시지로 받은 퀴즈 결과 저장 완료: {}", history);
        } catch (Exception e) {
            log.error("❌ 퀴즈 메시지 처리 실패", e);
        }
    }
}
