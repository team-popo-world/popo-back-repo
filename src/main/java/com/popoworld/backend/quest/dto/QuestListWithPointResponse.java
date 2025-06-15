package com.popoworld.backend.quest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestListWithPointResponse {
    private Integer currentPoint;
    private List<QuestResponse>quests;
}
