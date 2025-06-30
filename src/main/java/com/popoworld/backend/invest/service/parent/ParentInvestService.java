package com.popoworld.backend.invest.service.parent;

import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotEditRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotSetRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.request.SaveCustomScenarioRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.response.CustomScenarioListDTO;
import com.popoworld.backend.invest.dto.parent.dto.response.GetCustomScenarioListResponseDTO;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

public interface ParentInvestService {
    void deleteScenario(UUID scenarioId);
    CustomScenarioListDTO getScenarioList(UUID childId, String chapterId , PageRequest pageRequest);
    void setEditScenario(UUID userId, ChatbotSetRequestDTO requestDTO);
    void processChatMessage(UUID userId, ChatbotEditRequestDTO requestDTO, UUID requestId);

    void saveScenario(UUID userId, SaveCustomScenarioRequestDTO requestDTO);
}
