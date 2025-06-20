package com.popoworld.backend.invest.service.child;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.invest.dto.child.request.ClearChapterRequest;
import com.popoworld.backend.invest.dto.child.request.TurnDataRequest;
import com.popoworld.backend.invest.dto.child.response.ChapterDataResponse;
import com.popoworld.backend.invest.dto.child.response.ClearChapterResponse;
import com.popoworld.backend.invest.dto.child.response.TurnDataResponse;
import com.popoworld.backend.invest.entity.InvestHistory;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.entity.InvestSession;
import com.popoworld.backend.invest.investHistoryKafka.InvestHistoryKafkaProducer;
import com.popoworld.backend.invest.repository.InvestChapterRepository;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import com.popoworld.backend.invest.repository.InvestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final InvestChapterRepository investChapterRepository;

    @Transactional
    public ChapterDataResponse getChapterDataAndCreateSession(String chapterId){
        UUID childId = getCurrentUserId();

        //1. 유저 조회 및 시드머니 확인
        User child = userRepository.findById(childId)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));

        //2. 챕터 정보 조회하여 시드머니 확인
        var chapter = investChapterRepository.findById(chapterId)
                .orElseThrow(()->new RuntimeException("해당 챕터를 찾을 수 없습니다."));

        // 3. 포인트 충분한지 확인
        if (!child.hasEnoughPoints(chapter.getSeedMoney())) {
            throw new RuntimeException("포인트가 부족합니다. 필요 포인트: " + chapter.getSeedMoney() +
                    ", 보유 포인트: " + child.getPoint());
        }

        // 4. 시나리오 조회(우선순위: 해당 아이의 커스텀 시나리오 > 기본 시나리오)
        InvestScenario selectedScenario = null;

        // 4-1. 먼저 해당 아이의 커스텀 시나리오들 조회 (childId = 현재 사용자 & isCustom = true)
        List<InvestScenario> customScenarios = investScenarioRepository
                .findByChildIdAndInvestChapter_ChapterIdAndIsCustom(childId, chapterId, true);

        if(!customScenarios.isEmpty()){
            // 해당 아이의 커스텀 시나리오가 있으면 그 중에서 랜덤으로 선택
            Random random = new Random();
            int randomIndex = random.nextInt(customScenarios.size());
            selectedScenario = customScenarios.get(randomIndex);
        } else {
            // 4-2. 커스텀 시나리오가 없으면 기본 시나리오들 조회 (childId = null & isCustom = false)
            List<InvestScenario> defaultScenarios = investScenarioRepository
                    .findByChildIdIsNullAndInvestChapter_ChapterIdAndIsCustom(chapterId, false);

            if(!defaultScenarios.isEmpty()){
                // 기본 시나리오 중에서 랜덤으로 선택
                Random random = new Random();
                int randomIndex = random.nextInt(defaultScenarios.size());
                selectedScenario = defaultScenarios.get(randomIndex);
            }
        }

        // 시나리오를 찾지 못한 경우
        if(selectedScenario == null){
            throw new RuntimeException("해당 챕터 시나리오를 찾지 못했습니다.");
        }

        // 5. 시드머니 차감
        child.deductPoints(chapter.getSeedMoney());
        userRepository.save(child);

        // 6. 새로운 게임 세션 생성
        UUID sessionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        System.out.println("🔍 저장 전 - childId: " + childId);
        System.out.println("🔍 저장 전 - chapterId: " + chapterId);

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

        // 7. 세션 저장
        investSessionRepository.save(newSession);

        // 8. 응답 DTO 반환
        return new ChapterDataResponse(sessionId.toString(), selectedScenario.getStory());
    }


    @Transactional
    public ClearChapterResponse clearChapter(String chapterId, ClearChapterRequest request){
        UUID childId = getCurrentUserId();

        // 1. sessionId 변환
        UUID sessionId = UUID.fromString(request.getSessionId());

        // 2. 기존 세션 찾기
        InvestSession existingSession = investSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("해당 게임 세션을 찾을 수 없습니다."));

        // 3. 유저 조회
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 4. 챕터 정보 조회
        var chapter = investChapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("해당 챕터를 찾을 수 없습니다."));

        // 5. 포인트 정산
        if (request.getSuccess() != null && request.getSuccess()) {
            // 성공한 경우: 시드머니 + 수익 지급
            int totalReward = chapter.getSeedMoney() + (request.getProfit() != null ? request.getProfit() : 0);
            child.addPoints(totalReward);
        }
        // 실패하거나 중간에 나간 경우: 이미 차감된 시드머니는 돌려주지 않음

        userRepository.save(child);

        // 6. 기존 세션 업데이트
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

        // 7. 업데이트된 세션 저장
        investSessionRepository.save(updatedSession);

        // 8. 응답 DTO 반환
        String message = request.getSuccess() != null && request.getSuccess()
                ? "✅ 게임 성공! 포인트가 지급되었습니다."
                : "📝 게임 세션이 종료되었습니다.";

        return new ClearChapterResponse(message);
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


