package com.popoworld.backend.popoPet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedingResponse {
    private Integer newLevel; //먹이준 후 레벨
    private Integer currentExperience; //현재 경험치(다음레벨까지)
    private Integer totalExperience; //총 누적 경험치
    private Integer gainedExperience;   // 이번에 얻은 경험치
    private boolean levelUp;            // 레벨업 여부
    private List<String> fedItems;      // 먹인 아이템 이름들
}
