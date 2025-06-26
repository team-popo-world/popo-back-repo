package com.popoworld.backend.invest.service.child;

import com.popoworld.backend.invest.dto.child.request.CustomScenarioRequest;
import com.popoworld.backend.invest.dto.child.request.DefaultScenarioRequest;
import com.popoworld.backend.invest.entity.InvestChapter;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.repository.InvestChapterRepository;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final InvestScenarioRepository investScenarioRepository;
    private final InvestChapterRepository investChapterRepository;
    /**
     * ML에서 생성된 기본 시나리오 저장
     */
    public String createDefaultScenario(DefaultScenarioRequest request) {
        try {
            // 1. chapterId로 InvestChapter 엔티티 조회
            InvestChapter chapter = investChapterRepository.findById(request.getChapterId()).orElse(null);
            if (chapter == null) {
                throw new RuntimeException("해당 챕터 ID를 찾을 수 없습니다: " + request.getChapterId());
            }

            // 백엔드에서 설정하는 값들
            UUID scenarioId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            String scenarioName = generateNextScenarioName();

            // InvestScenario 객체 생성
            InvestScenario scenario = new InvestScenario(
                    scenarioId,
                    null,
                    scenarioName,
                    request.getStory(),
                    request.getSummary(),
                    request.getIsCustom(),
                    now,        // createdAt - 생성 시간
                    null,       // updatedAt - 생성 시에는 null
                    chapter,       // investChapter - chapterId로 연결할 수도 있음
                    new ArrayList<>() // investSessions
            );

            investScenarioRepository.save(scenario);

            return "✅ 시나리오가 성공적으로 저장되었습니다. ID: " + scenarioId;

        } catch (Exception e) {
            throw new RuntimeException("시나리오 저장 실패: " + e.getMessage());
        }
    }

    public String generateNextScenarioName() {
        Optional<String> lastNameOpt = investScenarioRepository.findLastScenarioName();
        int nextNumber = 1;

        if (lastNameOpt.isPresent()) {
            String lastName = lastNameOpt.get(); // 예: "기본-0023"
            String numberStr = lastName.replace("기본-", ""); // "0023"
            try {
                nextNumber = Integer.parseInt(numberStr) + 1; // 24
            } catch (NumberFormatException ignored) {}
        }

        return String.format("기본-%04d", nextNumber); // 예: "기본-0024"
    }

    /**
     * ML에서 생성된 커스텀 시나리오로 특정 챕터의 가장 오래된 시나리오 업데이트
     */
//    public String updateOldestScenario(CustomScenarioRequest request) {
//        try {
//            // 특정 chapterId 중에서 업데이트되지 않은 것 중 가장 오래된 시나리오 찾기
//            InvestScenario oldestScenario = investScenarioRepository
//                    .findTopByInvestChapter_ChapterIdAndUpdatedAtIsNullOrderByCreateAtAsc(request.getChapterId());
//
//            if (oldestScenario == null) {
//                throw new RuntimeException("챕터 " + request.getChapterId() + "에서 업데이트할 시나리오가 없습니다.");
//            }
//
//            // 시나리오 업데이트
//            LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
//
//            InvestScenario updatedScenario = new InvestScenario(
//                    oldestScenario.getScenarioId(),
//                    oldestScenario.getChildId(),
//                    request.getStory(),                      // 새로운 story
//                    request.getIsCustom(),                   // 새로운 isCustom
//                    oldestScenario.getCreateAt(),            // 기존 createdAt 유지
//                    now,                                     // updatedAt을 현재 시간으로 설정
//                    oldestScenario.getInvestChapter(),
//                    oldestScenario.getInvestSessions()
//            );
//
//            investScenarioRepository.save(updatedScenario);
//
//            return "✅ 챕터 " + request.getChapterId() + "의 가장 오래된 시나리오가 업데이트되었습니다. ID: " + oldestScenario.getScenarioId();
//
//        } catch (Exception e) {
//            throw new RuntimeException("시나리오 업데이트 실패: " + e.getMessage());
//        }
//    }
}