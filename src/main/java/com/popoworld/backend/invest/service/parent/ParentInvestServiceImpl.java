package com.popoworld.backend.invest.service.parent;

import com.popoworld.backend.invest.dto.parent.dto.GetCustomScenarioListResponseDTO;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentInvestServiceImpl implements ParentInvestService{

    private final InvestScenarioRepository investScenarioRepository;


    public void deleteScenario(UUID scenarioId) {
        // 존재 여부 확인 (예외 처리 가능)
        InvestScenario scenario = investScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 시나리오가 존재하지 않습니다."));

        // 삭제
        investScenarioRepository.delete(scenario);
    }

    public List<GetCustomScenarioListResponseDTO> getScenarioList(UUID childId, PageRequest pageRequest) {
        // 시나리오 리스트 가져온 후에
        List<InvestScenario> scenario =  investScenarioRepository.findByChildId(childId, pageRequest).getContent();
        // dto로 매핑
        return scenario.stream().map(s -> GetCustomScenarioListResponseDTO.builder().scenario(s).build()).toList();
    }
}
