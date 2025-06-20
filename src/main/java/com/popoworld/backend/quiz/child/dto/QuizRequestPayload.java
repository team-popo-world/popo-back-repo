package com.popoworld.backend.quiz.child.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizRequestPayload {
    private UUID userId;
    private String difficulty;
    private String topic;
}