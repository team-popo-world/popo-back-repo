package com.popoworld.backend.quiz.child.controller;

import com.popoworld.backend.global.token.JwtTokenProvider;
import com.popoworld.backend.invest.service.parent.ChatbotSseEmitters;
import com.popoworld.backend.quiz.child.service.QuizKafkaProducer;
import com.popoworld.backend.quiz.child.service.QuizService;
import com.popoworld.backend.quiz.child.service.QuizSseEmitters;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizKafkaProducer quizKafkaProducer;
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatbotSseEmitters sseEmitters;


    @Operation(summary = "퀴즈 요청" , description = "퀴즈 요청 api")
    @GetMapping
    public ResponseEntity<String> requestQuiz(
            @RequestParam String difficulty,
            @RequestParam String topic
    ) {
        String requestId = UUID.randomUUID().toString();
        UUID userId = getCurrentUserId();

        quizKafkaProducer.sendQuizRequest(requestId, userId, difficulty, topic);

        return ResponseEntity.ok(requestId); // 프론트는 이 requestId로 추후 결과 요청
    }

    @Operation(summary = "SSE 연결", description = "퀴즈 알림용 SSE 연결")
    @GetMapping("/sse")
    public SseEmitter connect(HttpServletRequest request) throws AccessDeniedException {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing or invalid token");
        }

        UUID userId = UUID.fromString(jwtTokenProvider.getUserIdFromToken(token.substring(7)));
        return sseEmitters.create(userId);
    }


}
