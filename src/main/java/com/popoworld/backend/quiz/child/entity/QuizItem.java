package com.popoworld.backend.quiz.child.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizItem {

    private String question;

    private List<String> options;

    private String correctAnswer;

    private String selectedAnswer;
}
