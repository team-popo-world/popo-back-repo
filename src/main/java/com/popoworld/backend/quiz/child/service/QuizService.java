package com.popoworld.backend.quiz.child.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quiz.child.dto.QuizJson;
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

    public void createDefaultQuiz(UUID childId) {
        List<Quiz> newQuiz = createDefaultQuizList(childId);
        log.info("questionJson={}", newQuiz.get(1));
        quizDataRepository.saveAll(newQuiz);
    }

    private List<Quiz> createDefaultQuizList(UUID childId) {
        List<Quiz> dailyQuiz = new ArrayList<>();
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "용돈", QuizJson.aEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "용돈", QuizJson.aMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "용돈", QuizJson.aHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "저축", QuizJson.bEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "저축", QuizJson.bMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "저축", QuizJson.bHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "소비", QuizJson.cEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "소비", QuizJson.cMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "소비", QuizJson.cHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "투자", QuizJson.dEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "투자", QuizJson.dMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "투자", QuizJson.dHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "은행", QuizJson.eEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "은행", QuizJson.eMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "은행", QuizJson.eHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "화폐", QuizJson.fEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "화폐", QuizJson.fMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "화폐", QuizJson.fHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "물가", QuizJson.gEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "물가", QuizJson.gMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "물가", QuizJson.gHard));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "easy", "시장", QuizJson.hEasy));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "medium", "시장", QuizJson.hMedium));
        dailyQuiz.add(Quiz.createDefaultQuiz(childId, "hard", "시장", QuizJson.hHard));
        return dailyQuiz;
    }

    public int getPoint(UUID userId, QuizRewardRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.addPoints(request.getPoint());
        userRepository.save(user);
        return user.getPoint();
    }


}

