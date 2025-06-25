package com.popoworld.backend.quiz.child.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResponseDTO {
    private String questionJson;
}
