package com.popoworld.backend.invest.dto.parent.dto;

import com.popoworld.backend.invest.entity.InvestScenario;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class GetCustomScenarioListResponseDTO {

    @Id
    private UUID scenarioId;

    private UUID childId;

    @Column(columnDefinition = "TEXT")
    private String story;

    private Boolean isCustom; //부모가 만든건지

    @Column(name = "create_at")  // DB 컬럼명과 맞춤
    private LocalDateTime createAt;

    @Column(name = "updated_at") // DB 컬럼명과 맞춤
    private LocalDateTime updatedAt;


    @Builder
    public GetCustomScenarioListResponseDTO(InvestScenario scenario) {
        this.scenarioId = scenario.getScenarioId();
        this.childId = scenario.getChildId();
        this.story = scenario.getStory();
        this.isCustom = scenario.getIsCustom();
        this.createAt = scenario.getCreateAt();
        this.updatedAt = scenario.getUpdatedAt();
    }
}
