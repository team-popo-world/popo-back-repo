package com.popoworld.backend.invest.service.child;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.invest.dto.child.request.ClearChapterRequest;
import com.popoworld.backend.invest.dto.child.request.TurnDataRequest;
import com.popoworld.backend.invest.dto.child.response.ChapterDataResponse;
import com.popoworld.backend.invest.dto.child.response.ClearChapterResponse;
import com.popoworld.backend.invest.dto.child.response.TurnDataResponse;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.entity.InvestSession;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import com.popoworld.backend.invest.repository.InvestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Service
@RequiredArgsConstructor
public class InvestService {

    private final InvestScenarioRepository investScenarioRepository;
    private final InvestSessionRepository investSessionRepository;
    private final InvestHistoryKafkaProducer investHistoryKafkaProducer;

    public ChapterDataResponse getChapterDataAndCreateSession(String chapterId){
        // 1. 시나리오 조회 (우선순위: 커스텀 시나리오 > 기본 시나리오)
        InvestScenario selectedScenario = null;

        //1-1. 먼저 커스텀 시나리오들 조회 (isCustom = true)
        List<InvestScenario>customScenarios = investScenarioRepository.findByInvestChapter_ChapterIdAndIsCustom(chapterId,true);

        if(!customScenarios.isEmpty()){
            //커스텀 시나리오가 있으면 그 중에서 랜덤으로 선택
            Random random = new Random();
            int randomIndex = random.nextInt(customScenarios.size());
            selectedScenario = customScenarios.get(randomIndex);
        }
        else{
            //1-2. 커스텀 시나리오가 없으면 기본 시나리오들 조회
            List<InvestScenario> defaultScenarios = investScenarioRepository.findByInvestChapter_ChapterIdAndIsCustom(chapterId,false);
            if(!defaultScenarios.isEmpty()){
                //기본 시나리오 중에서 랜덤으로 선택
                Random random = new Random();
                int randomIndex = random.nextInt(defaultScenarios.size());
                selectedScenario = defaultScenarios.get(randomIndex);
            }
            //시나리오를 찾지 못한 경우
            if(selectedScenario ==null){
                throw new RuntimeException("해당 챕터 시나리오를 찾지 못했습니다.");
            }
        }
        // 2. 새로운 게임 세션 생성
        UUID sessionId = UUID.randomUUID();
        UUID childId = getCurrentUserId();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        InvestSession newSession = new InvestSession(
                sessionId,        // 새로 생성한 세션 ID
                childId,          // 임시 childId
                chapterId,        // URL에서 받은 chapterId
                now,              // startedAt - 현재 시간
                null,             // endedAt - 아직 게임이 안 끝남
                null,             // success - 아직 모름
                null,             // profit - 아직 모름
                selectedScenario          // 조회한 scenario 객체
        );

        // 3. 세션 저장
        investSessionRepository.save(newSession);

        // 4. 응답 DTO 반환
        return new ChapterDataResponse(sessionId.toString(), selectedScenario.getStory());
    }

    public ClearChapterResponse clearChapter(String chapterId, ClearChapterRequest request){
        // 1. sessionId 변환
        UUID sessionId = UUID.fromString(request.getSessionId());

        // 2. 기존 세션 찾기
        InvestSession existingSession = investSessionRepository.findById(sessionId).orElse(null);

        if (existingSession == null) {
            throw new RuntimeException("해당 게임 세션을 찾을 수 없습니다.");
        }

        // 3. 기존 세션 업데이트
        LocalDateTime endedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        InvestSession updatedSession = new InvestSession(
                existingSession.getInvestSessionId(),  // 기존 세션 ID 유지
                existingSession.getChildId(),          // 기존 childId 유지
                existingSession.getChapterId(),        // 기존 chapterId 유지
                existingSession.getStartedAt(),        // 기존 시작 시간 유지
                endedAt,                               // 종료 시간은 현재 시간
                request.getSuccess(),                  // 프론트에서 받은 성공 여부
                request.getProfit(),                   // 프론트에서 받은 수익률
                existingSession.getInvestScenario()    // 기존 scenario 유지
        );

        // 4. 업데이트된 세션 저장
        investSessionRepository.save(updatedSession);

        // 5. 응답 DTO 반환
        return new ClearChapterResponse("✅ 게임 세션이 성공적으로 업데이트되었습니다.");

    }
    public TurnDataResponse updateGameData(String chapterId, Integer turn, TurnDataRequest request) {
        // 1. sessionId 변환
        UUID investSessionId = UUID.fromString(request.getSessionId());

        UUID childId = getCurrentUserId();

        // 3. 시간 파싱
        LocalDateTime startedAt = LocalDateTime.parse(request.getStartedAt());
        LocalDateTime endedAt = LocalDateTime.parse(request.getEndedAt());

        // 4. InvestHistory 객체 생성
        InvestHistory history = new InvestHistory(
                UUID.randomUUID(),
                investSessionId,
                chapterId,
                childId,
                turn,
                request.getRiskLevel(),
                request.getCurrentPoint(),
                request.getBeforeValue(),
                request.getCurrentValue(),
                request.getInitialValue(),
                request.getNumberOfShares(),
                request.getIncome(),
                request.getTransactionType(),
                request.getPlusClick(),
                request.getMinusClick(),
                request.getNewsTag(),
                startedAt,
                endedAt
        );

        // 5. 카프카로 전송
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(history);

            investHistoryKafkaProducer.sendInvestHistory("invest-history", json);

            return new TurnDataResponse("✅ 투자 데이터가 카프카로 전송되었습니다.");
        } catch (Exception e) {
            throw new RuntimeException("카프카 전송 실패: " + e.getMessage());
        }
    }
}


