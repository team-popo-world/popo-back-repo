package com.popoworld.backend.diary.dto;


import com.popoworld.backend.diary.enums.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryTodayRequest {
    private Emotion emotion;
    private String description;
}
