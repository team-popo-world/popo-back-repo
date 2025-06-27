package com.popoworld.backend.quiz.child.controller;

import com.popoworld.backend.global.token.JwtTokenProvider;
import com.popoworld.backend.invest.service.parent.ChatbotSseEmitters;
import com.popoworld.backend.quiz.child.dto.QuizResponseDTO;
import com.popoworld.backend.quiz.child.dto.QuizResultDTO;
import com.popoworld.backend.quiz.child.dto.QuizRewardRequestDTO;
import com.popoworld.backend.quiz.child.entity.Quiz;
import com.popoworld.backend.quiz.child.entity.QuizHistory;
import com.popoworld.backend.quiz.child.service.QuizKafkaProducer;
import com.popoworld.backend.quiz.child.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizKafkaProducer quizKafkaProducer;

    @Operation(summary = "퀴즈 요청 하루 한 번" , description = "퀴즈 요청 하루 한 번 api")
    @GetMapping("/active")
    public ResponseEntity<?> requestQuizActive(
    ) {
        try {
            UUID userId = getCurrentUserId();

            if (!quizKafkaProducer.isRequestAllowed(userId)) {
                throw new IllegalStateException("퀴즈는 하루에 한 번만 요청할 수 있습니다.");
            }

            quizKafkaProducer.markRequest(userId);

            return ResponseEntity.ok("요청 성공");
        }
        catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "퀴즈 요청" , description = "퀴즈 요청 api" +
            "difficulty = easy/medium/hard" +
            "topic = 한글 토픽 8개 중 하나")
    @GetMapping
    public ResponseEntity<QuizResponseDTO> requestQuiz(
            @RequestParam String difficulty,
            @RequestParam String topic
    ) {
  
        UUID userId = getCurrentUserId();
        QuizResponseDTO response = quizKafkaProducer.sendQuizRequest(userId, difficulty, topic);
        return ResponseEntity.ok(response);

    }


    @Operation(summary = "퀴즈 결과 저장" , description = "퀴즈 결과 저장 api")
    @PostMapping("/save")
    public ResponseEntity<?> saveQuizResult(@RequestBody QuizResultDTO request) {
        UUID userId = getCurrentUserId();

        quizService.saveQuizResult(userId, request);

        return ResponseEntity.ok("저장 성공");
    }

    @Operation(summary = "퀴즈 보상 획득" , description = "퀴즈 결과 보상 획득 api")
    @PostMapping("/point")
    public ResponseEntity<Integer> getPoint(@RequestBody QuizRewardRequestDTO request) {
        UUID userId = getCurrentUserId();

        Integer userPoint = quizService.getPoint(userId, request);

        return ResponseEntity.ok(userPoint);
    }


}
