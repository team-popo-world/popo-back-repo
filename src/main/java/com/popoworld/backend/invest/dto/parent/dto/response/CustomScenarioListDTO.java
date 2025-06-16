package com.popoworld.backend.invest.dto.parent.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomScenarioListDTO {
    private int totalPageSize;
    private List<GetCustomScenarioListResponseDTO> scenarioList;
}
