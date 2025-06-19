package com.popoworld.backend.quiz.child.controller;

import com.popoworld.backend.quiz.child.dto.QuizResponseDTO;
import com.popoworld.backend.quiz.child.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "퀴즈 요청" , description = "퀴즈 요청 api")
    @GetMapping
    public QuizResponseDTO getQuiz(
            @RequestParam String difficulty,
            @RequestParam String topic
    ) {
        return quizService.requestQuiz(difficulty, topic);
    }


}
