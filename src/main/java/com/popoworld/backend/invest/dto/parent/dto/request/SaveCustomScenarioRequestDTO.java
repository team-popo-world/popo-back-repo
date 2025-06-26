package com.popoworld.backend.invest.dto.parent.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SaveCustomScenarioRequestDTO {
    private UUID childId;
    private String scenarioName;
}
