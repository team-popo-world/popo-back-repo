package com.popoworld.backend.quiz.child.entity;


import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(collection = "invest_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizHistory {
    @Id
    private UUID id;

    @NotNull
    @Field(targetType = FieldType.STRING)
    private UUID userId; //사용자 구분

    private String difficulty; // 0 1 2

    private String topic;

    private List<QuizItem> questions;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;



}
