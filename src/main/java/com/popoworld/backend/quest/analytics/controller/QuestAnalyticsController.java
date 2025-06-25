package com.popoworld.backend.quest.analytics.controller;

import com.popoworld.backend.quest.analytics.client.QuestApiClient;
import com.popoworld.backend.quest.analytics.service.QuestAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/questAnalytics")
@Tag(name = "퀘스트 분석 API", description = "부모가 자녀의 퀘스트 수행 패턴을 분석할 수 있는 ML 기반 분석 데이터 제공")
@SecurityRequirement(name = "accessToken")
public class QuestAnalyticsController {

    private final QuestAnalyticsService analyticsService;
    private final QuestApiClient questApiClient;

    @GetMapping("/daily/completion-rate")
    @Operation(
            summary = "일일퀘스트 완료율 분석",
            description = """
                    **자녀의 일일퀘스트 완료율을 분석한 데이터를 조회합니다.**
                    
                    📊 **분석 내용:**
                    • 양치하기, 장난감 정리하기, 이불 개기, 식탁 정리하기, 하루 이야기 나누기
                    • 각 퀘스트별 완료율 (0.0 ~ 1.0)
                    • 주간/전체 기간별 선택 가능
                    
                    🎯 **활용 방법:**
                    • 자녀가 어떤 생활습관을 잘 지키는지 파악
                    • 완료율이 낮은 퀘스트에 대한 동기부여 방안 모색
                    • 일일퀘스트 난이도 조절 참고자료
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(
            name = "childId",
            description = "분석할 자녀의 UUID",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Parameter(
            name = "period",
            description = """
                    분석 기간 설정
                    
                    **recent7**: 최근 7일 데이터
                    **all**: 전체 기간 데이터 (기본값)
                    """,
            example = "all"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 일일퀘스트 완료율 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "일일퀘스트 완료율 응답 예시",
                                    value = """
                                    {
                                        "childid": "123e4567-e89b-12d3-a456-426614174000",
                                        "result": [
                                            { "quest_name": "정리", "completion_rate": 0.9 },
                                            { "quest_name": "양치", "completion_rate": 0.8 },
                                            { "quest_name": "수다", "completion_rate": 1.0 },
                                            { "quest_name": "이불", "completion_rate": 0.75 },
                                            { "quest_name": "식탁", "completion_rate": 0.82 }
                                        ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "❌ 접근 권한 없음 (다른 부모의 자녀 조회 시도)"),
            @ApiResponse(responseCode = "500", description = "❌ ML API 호출 실패")
    })
    public ResponseEntity<Object> getDailyCompletionRate(
            @RequestParam UUID childId,
            @RequestParam(defaultValue="all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getDailyCompletionRate(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("일일퀘스트 완료율 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parent/completion-rate")
    @Operation(
            summary = "부모퀘스트 완료율 분석",
            description = """
                    **자녀의 부모퀘스트 완료율을 라벨별로 분석한 데이터를 조회합니다.**
                    
                    📊 **분석 내용:**
                    • HABIT(생활습관), STUDY(학습), HOUSEHOLD(집안일), ERRAND(심부름), POPO(포포월드), ETC(기타)
                    • 각 라벨별 완료율 (0.0 ~ 1.0)
                    • 부모가 직접 만든 커스텀 퀘스트 분석
                    
                    🎯 **활용 방법:**
                    • 자녀가 어떤 유형의 퀘스트를 선호하는지 파악
                    • 완료율이 낮은 카테고리의 퀘스트 개선 방안 모색
                    • 향후 부모퀘스트 생성 시 참고자료
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (recent7/all)", example = "all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 부모퀘스트 완료율 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "부모퀘스트 완료율 응답 예시",
                                    value = """
                                    {
                                        "childid": "123e4567-e89b-12d3-a456-426614174000",
                                        "result": [
                                            { "quest_name": "HABIT", "completion_rate": 0.85 },
                                            { "quest_name": "STUDY", "completion_rate": 0.70 },
                                            { "quest_name": "HOUSEHOLD", "completion_rate": 0.90 },
                                            { "quest_name": "ERRAND", "completion_rate": 0.65 }
                                        ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "❌ 접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "❌ ML API 호출 실패")
    })
    public ResponseEntity<Object> getParentCompletionRate(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getParentCompletionRate(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("부모퀘스트 완료율 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/daily/completion-time")
    @Operation(
            summary = "일일퀘스트 완료 시간 분포 분석",
            description = """
                    **자녀가 일일퀘스트를 주로 언제 완료하는지 시간대별 분포를 분석한 데이터를 조회합니다.**
                    
                    ⏰ **분석 기준:**
                    • 자녀가 퀘스트 완료 요청(PENDING_APPROVAL)을 보낸 시점
                    • 2시간 단위로 구분: "0-2", "2-4", ..., "22-24"
                    • 각 퀘스트별 시간대별 완료 횟수
                    
                    📈 **차트 형태:**
                    • X축: 시간대 (24시간을 2시간씩 12구간)
                    • Y축: 완료 횟수
                    • 퀘스트별로 구분된 막대 그래프 또는 선 그래프
                    
                    🎯 **활용 방법:**
                    • 자녀의 일과 패턴 및 활동 시간 파악
                    • 퀘스트 알림 시간 최적화
                    • 생활 리듬 개선을 위한 가이드
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (weekly/all)", example = "all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 일일퀘스트 시간 분포 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "시간 분포 응답 예시",
                                    value = """
                                    {
                                        "childid": "123e4567-e89b-12d3-a456-426614174000",
                                        "result": [
                                            {
                                                "quest_name": "양치",
                                                "distribution": [
                                                    { "time_bin": "0-2", "count": 0 },
                                                    { "time_bin": "2-4", "count": 0 },
                                                    { "time_bin": "6-8", "count": 7 },
                                                    { "time_bin": "8-10", "count": 3 },
                                                    { "time_bin": "18-20", "count": 5 }
                                                ]
                                            }
                                        ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "❌ 접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "❌ ML API 호출 실패")
    })
    public ResponseEntity<Object> getDailyCompletionTime(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getDailyCompletionTime(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("일일퀘스트 시간분포 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parent/completion-time")
    @Operation(
            summary = "부모퀘스트 완료 시간 분포 분석",
            description = """
                    **자녀가 부모퀘스트를 주로 언제 완료하는지 시간대별 분포를 분석한 데이터를 조회합니다.**
                    
                    ⏰ **분석 기준:**
                    • 부모가 생성한 커스텀 퀘스트의 완료 시점
                    • 일일퀘스트와 달리 자유로운 시간에 수행되는 특성
                    • 2시간 단위 시간대별 분석
                    
                    🎯 **활용 방법:**
                    • 자녀가 집중해서 활동하는 시간대 파악
                    • 부모퀘스트 마감시간 설정 참고
                    • 가족 일정과의 조화 분석
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (weekly/all)", example = "all")
    public ResponseEntity<Object> getParentCompletionTime(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getParentCompletionTime(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("부모퀘스트 시간분포 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parent/completion-reward")
    @Operation(
            summary = "부모퀘스트 보상-완료율 상관관계 분석",
            description = """
                    **부모가 설정한 보상 포인트와 자녀의 퀘스트 완료율 간의 상관관계를 분석한 데이터를 조회합니다.**
                    
                    💰 **분석 내용:**
                    • 퀘스트 라벨별 평균 보상 포인트와 완료율
                    • 보상이 높을수록 완료율이 높아지는지 분석
                    • 회귀선(regression line)을 통한 추세 분석
                    
                    📊 **차트 형태:**
                    • X축: 보상 포인트
                    • Y축: 완료율 (0.0 ~ 1.0)
                    • 산점도(scatter plot) + 추세선
                    
                    🎯 **활용 방법:**
                    • 적정 보상 수준 가이드라인 설정
                    • 자녀 동기부여를 위한 보상 전략 수립
                    • 과도한 보상 지양 및 적절한 보상 체계 구축
                    
                    💡 **인사이트:**
                    • 보상이 너무 낮으면 동기부여 부족
                    • 보상이 너무 높으면 의존성 증가 우려
                    • 퀘스트 난이도에 맞는 적정 보상 찾기
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (weekly/all)", example = "all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 보상-완료율 상관관계 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "보상-완료율 분석 응답 예시",
                                    value = """
                                    {
                                        "childid": "123e4567-e89b-12d3-a456-426614174000",
                                        "result": [
                                            { "label": "HABIT", "reward": 30, "completion_rate": 0.75 },
                                            { "label": "STUDY", "reward": 50, "completion_rate": 0.60 },
                                            { "label": "HOUSEHOLD", "reward": 25, "completion_rate": 0.80 },
                                            { "label": "ERRAND", "reward": 20, "completion_rate": 0.90 }
                                        ],
                                        "regression_line": [
                                            { "reward": 20, "completion_rate": 0.91 },
                                            { "reward": 50, "completion_rate": 0.61 }
                                        ]
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "❌ 접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "❌ ML API 호출 실패")
    })
    public ResponseEntity<Object> getParentCompletionReward(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getParentCompletionReward(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("보상-완료율 분석 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/daily/approval-time")
    @Operation(
            summary = "일일퀘스트 부모 승인 소요시간 분석",
            description = """
                    **부모가 자녀의 일일퀘스트 완료 요청을 승인하기까지 걸리는 평균 시간을 분석한 데이터를 조회합니다.**
                    
                    ⏱️ **측정 기준:**
                    • PENDING_APPROVAL(승인 대기) → APPROVED(승인 완료) 상태 변경 시간
                    • 분 단위로 측정하여 평균 계산
                    • 사용자 친화적 포맷(X시간 Y분) 제공
                    
                    📊 **제공 정보:**
                    • avg_minutes: 평균 소요시간 (분 단위)
                    • formatted: 가독성 있는 시간 형식
                    
                    🎯 **활용 방법:**
                    • 부모의 피드백 속도 자가 점검
                    • 자녀 동기부여를 위한 빠른 피드백 필요성 인식
                    • 가족 소통 패턴 개선 포인트 파악
                    
                    💡 **권장사항:**
                    • 승인 시간이 너무 길면 자녀의 성취감 저하
                    • 적절한 승인 시간: 2-4시간 이내 권장
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (weekly/all)", example = "all")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 승인 소요시간 분석 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "승인 시간 응답 예시",
                                    value = """
                                    {
                                        "childid": "123e4567-e89b-12d3-a456-426614174000",
                                        "avg_minutes": 145.5,
                                        "formatted": "2시간 25분"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "❌ 접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "❌ ML API 호출 실패")
    })
    public ResponseEntity<Object> getDailyApprovalTime(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getDailyApprovalTime(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("일일퀘스트 승인시간 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parent/approval-time")
    @Operation(
            summary = "부모퀘스트 부모 승인 소요시간 분석",
            description = """
                    **부모가 자녀의 부모퀘스트 완료 요청을 승인하기까지 걸리는 평균 시간을 분석한 데이터를 조회합니다.**
                    
                    ⏱️ **측정 기준:**
                    • 부모가 직접 만든 커스텀 퀘스트의 승인 소요시간
                    • 일일퀘스트 대비 부모퀘스트 승인 패턴 비교 가능
                    • 퀘스트 중요도나 난이도에 따른 승인 시간 차이 분석
                    
                    🎯 **활용 방법:**
                    • 퀘스트 타입별 부모의 관심도 및 참여도 측정
                    • 자녀가 특별히 신경 써서 한 퀘스트에 대한 빠른 피드백 필요성
                    • 부모퀘스트의 교육적 효과 극대화를 위한 소통 개선
                    
                    💡 **비교 분석:**
                    • 일일퀘스트 vs 부모퀘스트 승인 시간 비교
                    • 퀘스트 라벨별 승인 시간 패턴 분석 가능
                    
                    🔒 **접근 권한:** 부모만 자신의 자녀 데이터 조회 가능
                    """
    )
    @Parameter(name = "childId", description = "분석할 자녀의 UUID", required = true)
    @Parameter(name = "period", description = "분석 기간 (weekly/all)", example = "all")
    public ResponseEntity<Object> getParentApprovalTime(
            @RequestParam UUID childId,
            @RequestParam(defaultValue = "all") String period) {
        try {
            UUID userId = getCurrentUserId();

            if (!analyticsService.hasAnalyticsAccess(userId, childId)) {
                return ResponseEntity.status(403).build();
            }

            Object response = questApiClient.getParentApprovalTime(childId, period);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("부모퀘스트 승인시간 프록시 실패: childId={}, period={}", childId, period, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}