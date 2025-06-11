package com.popoworld.backend.diary.child.dto;


import com.popoworld.backend.diary.child.enums.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryTodayRequest {
//    private String accessToken;
    private Emotion emotion;
    private String description;
}
