package com.popoworld.backend.popoPet.dto;

import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PopoFeedResponse {
    private Integer currentLevel;       // 포포 현재 레벨
    private Integer currentExperience;  // 현재 경험치
    private Integer totalExperience;    // 총 누적 경험치
    private List<InventoryItemResponse> availableFeeds; // 사용 가능한 먹이 목록
}
