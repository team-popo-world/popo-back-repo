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
@Tag(name = "저축통장 관리", description = "저축통장 생성, 조회, 매일입금 기능을 제공하는 API")
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
            summary = "저축통장 조회",
            description = "현재 활성화된 저축통장 정보를 조회합니다. 마감일이 지난 저축통장은 조회되지 않습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "저축통장 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountUserResponse.class),
                            examples = @ExampleObject(
                                    name = "저축통장 정보",
                                    value = """
                                    {
                                        "createdDate": "2025-06-15",
                                        "endDate": "2025-12-31",
                                        "goalAmount": 100000,
                                        "accountPoint": 25000,
                                        "currentPoint": 5000
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "활성화된 저축통장이 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "null")
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
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @Operation(
            summary = "매일입금",
            description = "저축통장에 포인트를 입금합니다. 사용자의 보유 포인트에서 차감되어 저축통장에 누적됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "입금 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyDepositResponse.class),
                            examples = @ExampleObject(
                                    name = "입금 성공",
                                    value = """
                                    {
                                        "currentPoint": "4000",
                                        "accountPoint": 26000
                                    }
                                    """
                            )
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
                                            value = """
                                            {
                                                "currentPoint": "보유 포인트가 부족합니다.",
                                                "accountPoint": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "만료된 저축통장",
                                            value = """
                                            {
                                                "currentPoint": "저축 통장이 만료되었습니다.",
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
                    description = "매일입금 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DailyDepositRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "일반 입금",
                                            value = """
                                            {
                                                "depositPoint": 1000,
                                                "success": false
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "목표 달성",
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