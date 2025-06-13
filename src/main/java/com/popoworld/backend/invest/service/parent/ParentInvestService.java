package com.popoworld.backend.invest.service.parent;

import com.popoworld.backend.invest.dto.parent.dto.GetCustomScenarioListResponseDTO;
import com.popoworld.backend.invest.entity.InvestScenario;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

public interface ParentInvestService {
    void deleteScenario(UUID scenarioId);
    List<GetCustomScenarioListResponseDTO> getScenarioList(UUID childId, PageRequest pageRequest);
}
