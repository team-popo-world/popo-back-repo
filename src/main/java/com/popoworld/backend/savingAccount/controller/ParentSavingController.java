package com.popoworld.backend.savingAccount.controller;

import com.popoworld.backend.savingAccount.dto.ParentDepositHistoryResponse;
import com.popoworld.backend.savingAccount.dto.ParentSavingAccountResponse;
import com.popoworld.backend.savingAccount.service.ParentSavingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/parent/saving")
@RequiredArgsConstructor
@Tag(name = "부모용 저축통장 조회", description = "부모가 자녀의 저축현황과 입금내역을 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
public class ParentSavingController {

    private final ParentSavingService parentSavingService;

    @Operation(
            summary = "자녀 저축통장 현황 조회",
            description = """
                    자녀의 저축통장 현재 상태를 조회합니다. (왼쪽 화면용)
                    
                    **화면 표시 정보:**
                    - 목표 저축 금액과 현재 저축 금액
                    - 목표 달성률 (원형 차트 표시용)
                    - 저축 기간 (시작일 ~ 마감일)
                    - 총 입금 횟수
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "저축통장 현황 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParentSavingAccountResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "goalAmount": 100000,
                                        "currentAccountPoint": 32000,
                                        "currentPercent": 32,
                                        "createdDate": "2025-06-20",
                                        "endDate": "2025-09-15",
                                        "status": "ACTIVE",
                                        "totalDepositCount": 8
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 자녀"),
            @ApiResponse(responseCode = "403", description = "부모-자녀 관계 확인 실패")
    })
    @GetMapping("/child/{childId}/account")
    public ResponseEntity<ParentSavingAccountResponse> getChildSavingAccount(
            @PathVariable UUID childId) {
        try {
            // TODO: 부모-자녀 관계 확인 로직 추가
            // UUID parentId = getCurrentUserId();
            // validateParentChildRelation(parentId, childId);

            ParentSavingAccountResponse response = parentSavingService.getChildSavingAccount(childId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
            summary = "자녀 저축 내역 전체 조회",
            description = """
                    자녀의 모든 입금 내역을 조회합니다. (오른쪽 화면용)
                    
                    **화면 표시 정보:**
                    - 날짜별 입금 리스트 (최신순)
                    - 각 입금: 날짜 + 자녀이름 + 입금액
                    - 프론트에서 무한스크롤 처리
                    
                    **데이터 크기**: 일반적으로 수십~수백 건으로 전체 조회 가능
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "저축 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    [
                                        {
                                            "depositAmount": 2100,
                                            "depositDate": "2025-07-19",
                                            "depositTime": "2025-07-19 14:30:00",
                                            "accountPointAfter": 32000,
                                            "childName": "아마카",
                                            "profileImage": null
                                        },
                                        {
                                            "depositAmount": 2000,
                                            "depositDate": "2025-07-18",
                                            "depositTime": "2025-07-18 09:15:00",
                                            "accountPointAfter": 29900,
                                            "childName": "아마카",
                                            "profileImage": null
                                        }
                                    ]
                                    """
                            )
                    )
            )
    })
    @GetMapping("/child/{childId}/deposits")
    public ResponseEntity<List<ParentDepositHistoryResponse>> getChildDepositHistory(
            @PathVariable UUID childId) {
        try {

            List<ParentDepositHistoryResponse> response = parentSavingService.getChildDepositHistory(childId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}