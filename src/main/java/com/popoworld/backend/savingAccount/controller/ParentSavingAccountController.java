package com.popoworld.backend.savingAccount.controller;

import com.popoworld.backend.savingAccount.dto.ParentSavingAccountDetailResponse;
import com.popoworld.backend.savingAccount.service.ParentSavingAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@Tag(name = "부모용 저축통장 API", description = "부모가 자녀의 모든 저축통장(현재+과거)을 조회하는 API")
@SecurityRequirement(name = "bearerAuth")
public class ParentSavingAccountController {

    private final ParentSavingAccountService parentSavingAccountService;

    @Operation(
            summary = "자녀의 모든 저축통장 조회",
            description = """
                    자녀의 모든 저축통장(현재 + 과거)을 최신순으로 조회합니다.
                    각 저축통장별로 입금내역도 함께 포함됩니다.
                    
                    **정렬 기준:**
                    - 저축통장: 종료일 기준 내림차순 (최신 통장이 첫 번째)
                    - 입금내역: 입금시간 기준 내림차순 (최근 입금이 첫 번째)
                    
                    **상태 구분:**
                    - ACTIVE: 현재 진행 중인 저축통장
                    - COMPLETED: 목표 달성 완료
                    - EXPIRED: 기간 만료
                    
                    **화면 구성:**
                    - 프론트에서 화살표로 저축통장별 탐색 가능
                    - 각 통장의 "저축내역 보기"로 해당 통장의 입금내역만 표시
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "저축통장 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                            [
                                                {
                                                    "goalAmount": 100000,
                                                    "currentAccountPoint": 32000,
                                                    "currentPercent": 32,
                                                    "createdDate": "2025-06-20",
                                                    "endDate": "2025-09-15",
                                                    "status": "ACTIVE",
                                                    "totalDepositCount": 8,
                                                    "deposits": [
                                                        {
                                                            "depositAmount": 2100,
                                                            "depositDate": "2025-07-19",
                                                            "depositTime": "2025-07-19 14:30:00",
                                                            "accountPointAfter": 32000,
                                                            "childName": "홍길동",
                                                            "profileImage": null
                                                        }
                                                    ]
                                                },
                                                {
                                                    "goalAmount": 50000,
                                                    "currentAccountPoint": 50000,
                                                    "currentPercent": 100,
                                                    "createdDate": "2025-05-01",
                                                    "endDate": "2025-06-01",
                                                    "status": "COMPLETED",
                                                    "totalDepositCount": 15,
                                                    "deposits": [
                                                        {
                                                            "depositAmount": 1000,
                                                            "depositDate": "2025-06-01",
                                                            "depositTime": "2025-06-01 18:00:00",
                                                            "accountPointAfter": 50000,
                                                            "childName": "홍길동",
                                                            "profileImage": null
                                                        }
                                                    ]
                                                }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "존재하지 않는 자녀"),
            @ApiResponse(responseCode = "403", description = "부모-자녀 관계 확인 실패")
    })
    @GetMapping("/child/{childId}/accounts")
    public ResponseEntity<List<ParentSavingAccountDetailResponse>> getAllChildSavingAccounts(
            @PathVariable UUID childId) {
        try {
            // TODO: 부모-자녀 관계 확인 로직 추가
            // UUID parentId = getCurrentUserId();
            // validateParentChildRelation(parentId, childId);

            List<ParentSavingAccountDetailResponse> response =
                    parentSavingAccountService.getAllChildSavingAccounts(childId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}