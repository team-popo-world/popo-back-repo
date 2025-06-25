package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestLabel;
import com.popoworld.backend.quiz.child.dto.QuizResultDTO;
import com.popoworld.backend.quiz.child.dto.QuizRewardRequestDTO;
import com.popoworld.backend.quiz.child.entity.Quiz;
import com.popoworld.backend.quiz.child.entity.QuizHistory;
import com.popoworld.backend.quiz.child.repository.QuizDataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final KafkaTemplate kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final QuizDataRepository quizDataRepository;

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

//    public void createDefaultQuiz(UUID childId) {
//        List<Quiz> newQuests = createDefaultQuizList(childId);
//        quizDataRepository.saveAll(newQuests);
//    }

//    private List<Quiz> createDefaultQuizList(UUID childId) {
//        List<Quiz> dailyQuiz = new ArrayList<>();
//        dailyQuiz.add(Quiz.createDailyQuest(childId, "easy", "물가", ));
//        return dailyQuiz;
//    }

    public int getPoint(UUID userId, QuizRewardRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.addPoints(request.getPoint());
        userRepository.save(user);
        return user.getPoint();
    }



}
