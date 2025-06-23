package com.popoworld.backend.quiz.child.dto;

import com.popoworld.backend.quiz.child.entity.QuizItem;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class QuizResultDTO {
    private String difficulty; // 0 1 2

    private String topic;

    private List<QuizItem> questions;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

}
