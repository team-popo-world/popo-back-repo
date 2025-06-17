package com.popoworld.backend.quest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentQuestRequest {
    private UUID childId;
    private String name;
    private String description;
    private Integer reward;

    @Schema(
            description = "마감 날짜 (YYYY-MM-DD 형식, 자동으로 해당 날짜의 23:59:59로 설정됨)",
            example = "2024-09-01",
            pattern = "^\\d{4}-\\d{2}-\\d{2}$"
    )
    private String endDate;

    private String imageUrl;
}
