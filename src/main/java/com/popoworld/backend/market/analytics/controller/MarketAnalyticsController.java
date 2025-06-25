package com.popoworld.backend.market.analytics.controller;

import com.popoworld.backend.market.analytics.client.MarketApiClient;
import com.popoworld.backend.market.analytics.service.MarketAnalyticsService;
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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "상점 대시보드 API", description = "ML 기반 자녀 대시보드 데이터 제공 - 소비패턴 및 상점 활동 분석")
@SecurityRequirement(name = "bearerAuth")
public class MarketAnalyticsController {

    private final MarketAnalyticsService analyticsService;
    private final MarketApiClient marketApiClient;

    @GetMapping("/{child_id}")
    @Operation(
            summary = "자녀 대시보드 데이터 조회",
            description = """
                    **ML에서 분석한 자녀의 종합 대시보드 데이터를 조회합니다.**
                    
                    🏠 **대시보드 구성:**
                    • 소비 패턴 및 상점 이용 현황
                    • 포인트 획득 및 사용 분석
                    • 선호 아이템 및 구매 패턴
                    • 절약 습관 및 소비 성향
                    
                    📊 **제공 데이터:**
                    • 포인트 현황 및 사용 통계
                    • 카테고리별 구매 선호도
                    • 시간대별 상점 이용 패턴
                    • 절약 vs 소비 성향 분석
                    
                    🎯 **활용 방안:**
                    • 자녀의 경제 관념 파악
                    • 현명한 소비 습관 지도
                    • 포인트 관리 능력 개발
                    • 금융 교육 방향 설정
                    
                    🔒 **접근 권한:** 
                    • 부모만 자신의 자녀 대시보드 조회 가능
                    • 자녀는 본인 대시보드 조회 불가 (부모용)
                    """
    )
    @Parameter(
            name = "child_id",
            description = "조회할 자녀의 UUID",
            required = true,
            example = "8a2b94b0-8395-41ad-99b0-6e8c0ea7edaa"
    )
    @Parameter(
            name = "days",
            description = """
                    분석할 일수 설정
                    
                    **범위:** 1일 ~ 365일
                    **기본값:** 7일 (최근 1주일)
                    **권장값:**
                    • 7일: 최근 단기 패턴 분석
                    • 30일: 월간 소비 트렌드
                    • 90일: 계절별 변화 패턴
                    • 365일: 연간 성장 추이
                    """,
            example = "7"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 대시보드 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "상점 대시보드 응답 예시",
                                            description = "ML에서 분석한 자녀의 소비패턴 대시보드",
                                            value = """
                                            {
                                                "childId": "8a2b94b0-8395-41ad-99b0-6e8c0ea7edaa",
                                                "period": {
                                                    "days": 7,
                                                    "startDate": "2025-06-18",
                                                    "endDate": "2025-06-25"
                                                },
                                                "pointsSummary": {
                                                    "currentPoints": 2850,
                                                    "earnedPoints": 1200,
                                                    "spentPoints": 750,
                                                    "savingRate": 0.375
                                                },
                                                "spendingPattern": {
                                                    "categories": [
                                                        { "name": "장난감", "amount": 350, "percentage": 46.7 },
                                                        { "name": "문구용품", "amount": 200, "percentage": 26.7 },
                                                        { "name": "간식", "amount": 150, "percentage": 20.0 },
                                                        { "name": "기타", "amount": 50, "percentage": 6.6 }
                                                    ],
                                                    "averageTransactionSize": 125,
                                                    "transactionCount": 6
                                                },
                                                "timePattern": {
                                                    "mostActiveHour": "16-18",
                                                    "preferredDays": ["토요일", "일요일"],
                                                    "impulseRatio": 0.25
                                                },
                                                "insights": {
                                                    "spendingPersonality": "신중한 소비자",
                                                    "strongPoints": ["계획적 구매", "비교구매"],
                                                    "improvementAreas": ["충동구매 줄이기"],
                                                    "recommendations": [
                                                        "용돈 기입장 작성 습관",
                                                        "목표 저축액 설정하기"
                                                    ]
                                                },
                                                "trends": {
                                                    "weeklyChange": "+15%",
                                                    "categoryTrend": "장난감 선호도 증가",
                                                    "savingTrend": "저축률 상승"
                                                }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "빈 데이터 응답 예시",
                                            description = "분석할 충분한 데이터가 없는 경우",
                                            value = """
                                            {
                                                "childId": "8a2b94b0-8395-41ad-99b0-6e8c0ea7edaa",
                                                "period": {
                                                    "days": 7,
                                                    "startDate": "2025-06-18",
                                                    "endDate": "2025-06-25"
                                                },
                                                "message": "분석할 충분한 데이터가 없습니다",
                                                "suggestion": "더 많은 상점 활동 후 다시 확인해보세요"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "❌ 잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "잘못된 days 파라미터",
                                    value = """
                                    {
                                        "error": "Bad Request",
                                        "message": "days 파라미터는 1~365 사이의 값이어야 합니다",
                                        "invalidValue": 400
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "❌ 접근 권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "권한 없음",
                                    value = """
                                    {
                                        "error": "Forbidden",
                                        "message": "해당 자녀의 대시보드에 접근할 권한이 없습니다"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "❌ 자녀를 찾을 수 없음"),
            @ApiResponse(
                    responseCode = "500",
                    description = "❌ ML API 호출 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                    {
                                        "error": "Internal Server Error",
                                        "message": "ML 서버와의 통신 중 오류가 발생했습니다"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<Object> getDashboard(
            @PathVariable("child_id") UUID childId,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            UUID userId = getCurrentUserId();

            // 🔒 권한 체크 (부모만 가능)
            if (!analyticsService.hasDashboardAccess(userId, childId)) {
                log.warn("대시보드 접근 권한 없음: userId={}, childId={}", userId, childId);
                return ResponseEntity.status(403).build();
            }

            // 📊 days 파라미터 검증 (1~365일)
            if (days < 1 || days > 365) {
                log.warn("잘못된 days 파라미터: userId={}, childId={}, days={}", userId, childId, days);
                return ResponseEntity.badRequest().build();
            }

            Object dashboardData = marketApiClient.getDashboardData(childId, days);

            log.info("✅ 대시보드 조회 성공: userId={}, childId={}, days={}", userId, childId, days);
            return ResponseEntity.ok(dashboardData);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: childId={}, days={}, error={}", childId, days, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("대시보드 조회 실패: childId={}, days={}, error={}", childId, days, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}