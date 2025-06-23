package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quiz.child.dto.QuizResultDTO;
import com.popoworld.backend.quiz.child.dto.QuizRewardRequestDTO;
import com.popoworld.backend.quiz.child.entity.QuizHistory;
import com.popoworld.backend.quiz.child.repository.QuizRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

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

    public int getPoint(UUID userId, QuizRewardRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.addPoints(request.getPoint());
        userRepository.save(user);
        return user.getPoint();
    }

}
