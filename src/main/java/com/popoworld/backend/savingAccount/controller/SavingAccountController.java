package com.popoworld.backend.savingAccount.controller;

import com.popoworld.backend.savingAccount.dto.AccountCreateRequest;
import com.popoworld.backend.savingAccount.dto.AccountUserResponse;
import com.popoworld.backend.savingAccount.dto.DailyDepositRequest;
import com.popoworld.backend.savingAccount.dto.DailyDepositResponse;
import com.popoworld.backend.savingAccount.service.SavingAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/saveAccount")
@RequiredArgsConstructor
@Tag(name = "Child Saving Account", description = "저축통장 생성, 조회, 매일입금 기능을 제공하는 API")
@SecurityRequirement(name = "bearerAuth")
public class SavingAccountController {

    private final SavingAccountService savingAccountService;

    @Operation(
            summary = "저축통장 생성",
            description = "새로운 저축통장을 생성합니다. 사용자는 하나의 활성 저축통장만 가질 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "저축통장 생성 성공",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "저축 통장이 성공적으로 생성되었습니다.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이미 활성 저축통장 존재, 유효하지 않은 데이터 등)",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = {
                                    @ExampleObject(name = "중복 저축통장", value = "이미 활성화된 저축 통장이 있습니다. 하나의 저축 통장만 사용할 수 있습니다."),
                                    @ExampleObject(name = "잘못된 목표금액", value = "목표 금액은 0보다 커야 합니다."),
                                    @ExampleObject(name = "잘못된 날짜", value = "종료 날짜는 시작 날짜보다 뒤여야 합니다.")
                            }
                    )
            )
    })
    @PostMapping
    public ResponseEntity<String> createSavingAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저축통장 생성 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "저축통장 생성 예시",
                                    value = """
                                    {
                                        "goalAmount": 100000,
                                        "createdAt": "2025-06-15",
                                        "endDate": "2025-12-31",
                                        "rewardPoint": 10000
                                    }
                                    """
                            )
                    )
            )
            @RequestBody AccountCreateRequest request) {
        try {
            UUID childId = getCurrentUserId();
            String result = savingAccountService.createSavingAccount(childId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("잘못된 요청");
        }
    }

    @Operation(
            summary = "저축통장 상태 조회",
            description = """
                    저축통장의 현재 상태를 조회합니다. 
                    
                    **상태별 설명:**
                    - **ACTIVE**: 현재 진행 중인 저축통장 (정상 사용 가능)
                    - **ACHIEVED**: 목표를 달성한 저축통장 (보상 지급 완료)
                    - **EXPIRED**: 기간이 만료된 저축통장 (저축금만 반환)
                    - **NONE**: 저축통장이 없음 (새로 생성 필요)
                    - **ERROR**: 조회 중 오류 발생
                    
                    **프론트엔드 처리 가이드:**
                    1. `status`가 `ACTIVE`면 정상 저축통장 화면 표시
                    2. `status`가 `ACHIEVED`면 저축통장 정보 + 달성 축하 모달 표시
                    3. `status`가 `EXPIRED`면 저축통장 정보 + 만료 안내 모달 표시  
                    4. `status`가 `NONE`이면 저축통장 생성 페이지로 이동
                    5. `returnedPoints`는 달성/만료 시 받은 포인트 총액
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "저축통장 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountUserResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "활성 저축통장",
                                            description = "현재 진행 중인 저축통장 - 정상 화면 표시",
                                            value = """
                                            {
                                                "createdDate": "2025-06-15",
                                                "endDate": "2025-12-31",
                                                "goalAmount": 100000,
                                                "accountPoint": 45000,
                                                "currentPoint": 8500,
                                                "status": "ACTIVE",
                                                "returnedPoints": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "목표 달성한 저축통장",
                                            description = "목표 달성 완료 - 저축통장 배경 + 축하 모달 표시",
                                            value = """
                                            {
                                                "createdDate": "2025-06-01",
                                                "endDate": "2025-06-15",
                                                "goalAmount": 100000,
                                                "accountPoint": 100000,
                                                "currentPoint": 25000,
                                                "status": "ACHIEVED",
                                                "returnedPoints": 110000
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "만료된 저축통장",
                                            description = "기간 만료 - 저축통장 배경 + 만료 안내 모달 표시",
                                            value = """
                                            {
                                                "createdDate": "2025-05-01",
                                                "endDate": "2025-06-10",
                                                "goalAmount": 100000,
                                                "accountPoint": 75000,
                                                "currentPoint": 80000,
                                                "status": "EXPIRED",
                                                "returnedPoints": 75000
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "저축통장 없음",
                                            description = "저축통장이 없음 - 생성 페이지로 이동",
                                            value = """
                                            {
                                                "createdDate": null,
                                                "endDate": null,
                                                "goalAmount": null,
                                                "accountPoint": null,
                                                "currentPoint": 15000,
                                                "status": "NONE",
                                                "returnedPoints": null
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "조회 중 오류 발생",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "오류 응답",
                                    value = """
                                    {
                                        "createdDate": null,
                                        "endDate": null,
                                        "goalAmount": null,
                                        "accountPoint": null,
                                        "currentPoint": null,
                                        "status": "ERROR",
                                        "returnedPoints": null
                                    }
                                    """
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<AccountUserResponse> getSavingAccount() {
        try {
            UUID childId = getCurrentUserId();
            AccountUserResponse response = savingAccountService.getSavingAccount(childId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(AccountUserResponse.builder()
                            .status("ERROR")
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AccountUserResponse.builder()
                            .status("ERROR")
                            .build());
        }
    }

    @Operation(
            summary = "매일입금",
            description = """
                    저축통장에 포인트를 입금합니다. 
                    
                    **입금 처리 과정:**
                    1. 사용자의 보유 포인트에서 입금 포인트 차감
                    2. 저축통장에 포인트 누적
                    3. 목표 달성 여부 확인 (`success: true`인 경우)
                    4. 달성 시: 저축금 + 보상금 지급 후 계좌 비활성화
                    
                    **주의사항:**
                    - 만료된 저축통장에는 입금 불가
                    - 보유 포인트 부족 시 입금 불가
                    - `success: true`는 프론트에서 목표 달성 계산 후 전송
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "입금 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyDepositResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "일반 입금 성공",
                                            description = "정상적인 입금 처리",
                                            value = """
                                            {
                                                "currentPoint": "7500",
                                                "accountPoint": 46000
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "목표 달성 입금",
                                            description = "목표 달성과 함께 입금 - 보상 지급 완료",
                                            value = """
                                            {
                                                "currentPoint": "125000",
                                                "accountPoint": 100000
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "입금 실패 (포인트 부족, 만료된 저축통장 등)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyDepositResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "포인트 부족",
                                            description = "사용자의 보유 포인트가 입금 포인트보다 적음",
                                            value = """
                                            {
                                                "currentPoint": "보유 포인트가 부족합니다.",
                                                "accountPoint": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "만료된 저축통장",
                                            description = "저축통장이 만료되어 입금 불가 (저축금은 자동 반환됨)",
                                            value = """
                                            {
                                                "currentPoint": "저축 통장이 만료되었습니다.",
                                                "accountPoint": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "활성 저축통장 없음",
                                            description = "입금할 수 있는 활성 저축통장이 없음",
                                            value = """
                                            {
                                                "currentPoint": "활성화된 저축 통장이 존재하지 않습니다.",
                                                "accountPoint": null
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PutMapping("/dailyDeposit")
    public ResponseEntity<DailyDepositResponse> dailyDeposit(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            매일입금 요청 정보
                            
                            **필드 설명:**
                            - `depositPoint`: 입금할 포인트 (양수 필수)
                            - `success`: 목표 달성 여부 (프론트에서 계산 후 전송)
                              - `false` 또는 `null`: 일반 입금
                              - `true`: 이번 입금으로 목표 달성 (보상 지급)
                            """,
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyDepositRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "일반 입금",
                                            description = "목표 달성하지 않은 일반적인 입금",
                                            value = """
                                            {
                                                "depositPoint": 1000,
                                                "success": false
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "목표 달성 입금",
                                            description = "이번 입금으로 목표 금액 달성 (보상 지급)",
                                            value = """
                                            {
                                                "depositPoint": 5000,
                                                "success": true
                                            }
                                            """
                                    )
                            }
                    )
            )
            @RequestBody DailyDepositRequest request) {
        try {
            UUID childId = getCurrentUserId();
            DailyDepositResponse response = savingAccountService.dailyDeposit(childId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new DailyDepositResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new DailyDepositResponse("잘못된 요청", null));
        }
    }
}