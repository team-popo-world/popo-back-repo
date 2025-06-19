package com.popoworld.backend.quiz.child.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class QuizItemDTO {
    private String question;
    private List<String> options;
    private int answerIndex;
}
