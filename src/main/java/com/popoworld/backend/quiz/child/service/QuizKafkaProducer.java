package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.quiz.child.dto.QuizRequestPayload;
import com.popoworld.backend.quiz.child.dto.QuizResponseDTO;
import com.popoworld.backend.quiz.child.entity.Quiz;
import com.popoworld.backend.quiz.child.repository.QuizDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuizKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final QuizDataRepository quizDataRepository;

    public QuizResponseDTO sendQuizRequest(UUID userId, String difficulty, String topic) {
        Optional<Quiz> quiz = quizDataRepository.findByUserIdAndDifficultyAndTopic(userId, difficulty, topic);

        QuizResponseDTO response = quiz
                .map(q -> QuizResponseDTO.builder().questionJson(q.getQuestionJson()).build())
                .orElse(QuizResponseDTO.builder().questionJson(null).build());

        try {
            QuizRequestPayload payload = new QuizRequestPayload(userId, difficulty, topic);
            String json = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send("quiz.request", userId.toString(), json);
            log.info("[Kafka] 퀴즈 요청 전송 완료: {}", json);
            return response;
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 직렬화 실패", e);
            throw new RuntimeException("Kafka 메시지 전송 실패", e);
        }
    }
}
