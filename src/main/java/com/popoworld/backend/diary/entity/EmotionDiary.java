package com.popoworld.backend.diary.entity;

import com.popoworld.backend.diary.enums.Emotion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Table(name="emotion_diary")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDiary {
    @Id
    @GeneratedValue
    private UUID emotionDiaryId;

    private UUID childId;

    private String description;

    @Enumerated(EnumType.STRING)
    private Emotion emotion;

    private LocalDate createdAt;

    @PrePersist
    protected void onCreate(){
        if(createdAt==null){
            createdAt = LocalDate.now();
        }
    }
}
