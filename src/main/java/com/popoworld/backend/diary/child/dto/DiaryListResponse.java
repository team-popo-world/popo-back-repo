package com.popoworld.backend.diary.child.dto;

import com.popoworld.backend.diary.child.enums.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@Getter
@AllArgsConstructor
@Builder
public class DiaryListResponse {
    private UUID emotionDiaryId;
    private UUID childId;
    private Emotion emotion;
    private String description;
    private LocalDate createdAt;
}
