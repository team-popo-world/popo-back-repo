package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.quiz.child.dto.QuizResultDTO;
import com.popoworld.backend.quiz.child.entity.QuizHistory;
import com.popoworld.backend.quiz.child.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void saveQuizResult(UUID userId, QuizResultDTO request) {
        try {
            QuizHistory quizHistory = new QuizHistory(
                    UUID.randomUUID(),
                    userId,
                    request.getDifficulty(),
                    request.getTopic(),
                    request.getQuestions(),
                    request.getStartedAt(),
                    request.getEndedAt()
            );

            String json = objectMapper.writeValueAsString(quizHistory);

            kafkaTemplate.send("quiz-history", json);
            log.info("✅ 퀴즈 결과가 Kafka로 전송됨");
        } catch (Exception e) {
            throw new RuntimeException("Kafka 전송 실패: " + e.getMessage());
        }
    }
}
