package com.popoworld.backend.invest.dto.parent.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DeleteCustomScenarioRequestDTO {
    private UUID scenarioId;
}
