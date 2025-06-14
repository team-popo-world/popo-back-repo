package com.popoworld.backend.invest.dto.parent.dto.response;

import com.popoworld.backend.invest.entity.InvestScenario;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class GetCustomScenarioListResponseDTO {

    private UUID scenarioId;

    private String story;

    private Boolean isCustom;

    private LocalDateTime createAt;

    private LocalDateTime updatedAt;


    @Builder
    public GetCustomScenarioListResponseDTO(InvestScenario scenario) {
        this.scenarioId = scenario.getScenarioId();
        this.story = scenario.getStory();
        this.isCustom = scenario.getIsCustom();
        this.createAt = scenario.getCreateAt();
        this.updatedAt = scenario.getUpdatedAt();
    }
}
