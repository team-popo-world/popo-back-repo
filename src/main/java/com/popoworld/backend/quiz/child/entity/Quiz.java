package com.popoworld.backend.quiz.child.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Builder
@Table(name = "quiz")
public class Quiz {

    @Id
    private UUID id;

    private UUID userId; //사용자 구분

    private String difficulty; // easy, medium, hard

    private String topic;

    @Column(columnDefinition = "TEXT")
    private String questionJson; // JSON 전체를 문자열로 저장

    public static Quiz createDefaultQuiz(UUID childId, String difficulty, String topic, String questionJson) {
        return Quiz.builder()
                .id(UUID.randomUUID())
                .userId(childId)
                .difficulty(difficulty)
                .topic(topic)
                .questionJson(questionJson)
                .build();
    }
}
