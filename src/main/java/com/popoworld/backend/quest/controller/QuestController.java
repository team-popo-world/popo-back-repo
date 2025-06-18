package com.popoworld.backend.quest.controller;

import com.popoworld.backend.quest.dto.*;
import com.popoworld.backend.quest.service.QuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/quest")
@RequiredArgsConstructor
@Tag(name = "퀘스트 관리 API", description = "포포월드 퀘스트 시스템 - 일일퀘스트와 부모퀘스트 관리")
@SecurityRequirement(name = "bearerAuth")
public class QuestController {
    private final QuestService questService;

    @GetMapping
    @Operation(
            summary = "퀘스트 목록 조회 (자녀용)",
            description = """
                    **자녀가 자신의 퀘스트 목록과 포인트를 조회합니다.**
                    
                    📋 **퀘스트 타입:**
                    • **일일퀘스트** (`daily`): 매일 자정에 자동 생성되는 기본 퀘스트
                      - 양치하기, 장난감 정리하기, 이불 개기 등
                      - 보상: 100포인트씩
                    
                    • **부모퀘스트** (`parent`): 부모가 자녀를 위해 만든 커스텀 퀘스트
                      - 숙제, 집안일, 특별한 목표 등
                      - 보상: 부모가 설정한 포인트
                    
                    🎮 **퀘스트 상태 흐름:**
                    1. `PENDING_ACCEPT` - 수락 대기 (생성된 직후)
                    2. `IN_PROGRESS` - 진행중 (자녀가 수락)
                    3. `PENDING_APPROVAL` - 승인 대기 (자녀가 완료 요청)
                    4. `APPROVED` - 승인 완료 (부모가 승인, 보상받기 가능)
                    5. `COMPLETED` - 최종 완료 (자녀가 보상받기 클릭)
                    6. `EXPIRED` - 만료 (부모퀘스트만, 시간 초과)
                    
                    💰 **포인트 시스템:**
                    • 퀘스트 완료시 자동으로 포인트 지급
                    • 포인트로 NPC 상점과 부모 상점에서 아이템 구매 가능
                    """
    )
    @Parameter(
            name = "type",
            description = """
                    조회할 퀘스트 타입을 필터링합니다.
                    
                    **허용값:**
                    • `daily` - 일일퀘스트만 조회
                    • `parent` - 부모퀘스트만 조회
                    • **미입력** - 모든 퀘스트 조회
                    """,
            example = "daily"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 퀘스트 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "일일퀘스트 목록",
                                            description = "매일 생성되는 기본 퀘스트들",
                                            value = """
                                            {
                                                "currentPoint": 10100,
                                                "quests": [
                                                    {
                                                        "quest_id": "643dfa5d-2794-41f0-8eef-8d98054cf2df",
                                                        "child_id": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                                        "type": "daily",
                                                        "name": "장난감 정리하기",
                                                        "description": "가지고 온 장난감은 스스로 치워볼까?",
                                                        "state": "PENDING_ACCEPT",
                                                        "end_date": "2025-06-15T23:59:59",
                                                        "created": "2025-06-15T15:40:49.984902",
                                                        "isStatic": false,
                                                        "reward": 100,
                                                        "imageUrl": null,
                                                        "label": "HOUSEHOLD"
                                                    },
                                                    {
                                                        "quest_id": "09810a61-69f4-4a68-a9df-6942d54abc0a",
                                                        "child_id": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                                        "type": "daily",
                                                        "name": "양치하기",
                                                        "description": "밥 먹었으면 포포와 양치하자!",
                                                        "state": "COMPLETED",
                                                        "end_date": "2025-06-15T23:59:59",
                                                        "created": "2025-06-15T15:40:49.984852",
                                                        "isStatic": false,
                                                        "reward": 100,
                                                        "imageUrl": null,
                                                        "label": "HABIT"
                                                    }
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "부모퀘스트 목록",
                                            description = "부모가 생성한 커스텀 퀘스트들",
                                            value = """
                                            {
                                                "currentPoint": 10100,
                                                "quests": [
                                                    {
                                                        "quest_id": "550e8400-e29b-41d4-a716-446655440003",
                                                        "child_id": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                                        "type": "parent",
                                                        "name": "숙제 완료하기",
                                                        "description": "이번 주 수학 숙제를 모두 완료해보자!",
                                                        "state": "IN_PROGRESS",
                                                        "end_date": "2025-06-20T23:59:59",
                                                        "created": "2025-06-15T10:30:00",
                                                        "isStatic": false,
                                                        "reward": 300,
                                                        "imageUrl": "https://example.com/homework.jpg",
                                                        "label": "STUDY"
                                                    }
                                                ]
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 타입 파라미터"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (로그인 필요)")
    })
    public ResponseEntity<QuestListWithPointResponse> getQuests(
            @RequestParam(required = false) String type
    ){
        try{
            UUID childId = getCurrentUserId();
            QuestListWithPointResponse response = questService.getQuestsWithPoint(childId, type);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/state")
    @Operation(
            summary = "퀘스트 상태 변경",
            description = """
                    **퀘스트의 상태를 단계별로 변경합니다.**
                    
                    🔄 **상태 변경 규칙:**
                    • `PENDING_ACCEPT` → `IN_PROGRESS` (자녀가 퀘스트 수락)
                    • `IN_PROGRESS` → `PENDING_APPROVAL` (자녀가 완료 신청)
                    • `PENDING_APPROVAL` → `APPROVED` (부모가 승인)
                    • `APPROVED` → `COMPLETED` (자녀가 보상받기)
                    
                    ⚠️ **주의사항:**
                    • 완료된 퀘스트는 되돌릴 수 없습니다
                    • 만료된 부모퀘스트는 상태 변경 불가
                    • `COMPLETED` 상태로 변경시 자동으로 포인트 지급
                    
                    💰 **포인트 지급:**
                    • 퀘스트가 `COMPLETED` 상태가 되면 즉시 포인트 지급
                    • 지급된 포인트는 즉시 사용 가능
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "퀘스트 상태 변경 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = QuestStateChangeRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "퀘스트 수락",
                                    description = "자녀가 퀘스트를 수락할 때",
                                    value = """
                                    {
                                        "questId": "09810a61-69f4-4a68-a9df-6942d54abc0a",
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "state": "IN_PROGRESS"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "완료 신청",
                                    description = "자녀가 퀘스트 완료를 신청할 때",
                                    value = """
                                    {
                                        "questId": "09810a61-69f4-4a68-a9df-6942d54abc0a",
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "state": "PENDING_APPROVAL"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "보상 받기",
                                    description = "자녀가 최종적으로 보상을 받을 때 (포인트 지급됨)",
                                    value = """
                                    {
                                        "questId": "09810a61-69f4-4a68-a9df-6942d54abc0a",
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "state": "COMPLETED"
                                    }
                                    """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ 상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 상태 또는 변경 규칙 위반"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "404", description = "❌ 퀘스트를 찾을 수 없음")
    })
    public ResponseEntity<String> changeQuestState(@RequestBody QuestStateChangeRequest request){
        try{
            questService.changeQuestState(request);
            return ResponseEntity.ok("퀘스트 상태가 변경되었습니다.");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @PostMapping("/create")
    @Operation(
            summary = "부모 퀘스트 생성",
            description = """
                    **부모가 자녀를 위한 커스텀 퀘스트를 생성합니다.**
                    
                    🏷️ **퀘스트 라벨 종류:**
                    • `HABIT` - 생활습관 (양치하기, 정리정돈, 일찍 일어나기 등)
                    • `STUDY` - 학습 (숙제, 독서, 공부, 시험 준비 등)
                    • `HOUSEHOLD` - 집안일 (청소, 설거지, 빨래 정리 등)
                    • `ERRAND` - 심부름 (쇼핑, 물건 가져다주기, 편지 전달 등)
                    • `POPO` - 포포월드 기능 (게임 내 특별 활동)
                    • `ETC` - 기타 (위 분류에 맞지 않는 활동)
                    
                    📅 **마감날짜 설정:**
                    • `YYYY-MM-DD` 형식으로 입력 (예: "2024-09-15")
                    • 자동으로 해당 날짜의 23:59:59로 설정됨
                    • 마감시간이 지나면 자동으로 `EXPIRED` 상태로 변경
                    
                    💰 **보상 포인트:**
                    • 퀘스트 난이도와 중요도에 맞게 설정
                    • 일일퀘스트(100P)보다 높게 설정 권장
                    • 완료시 자녀에게 즉시 지급
                    
                    📸 **이미지 URL:**
                    • 퀘스트를 시각적으로 표현할 이미지 (선택사항)
                    • 자녀의 이해를 돕는 그림이나 사진
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "부모 퀘스트 생성 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ParentQuestRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "학습 퀘스트",
                                    description = "공부 관련 퀘스트 생성 예시",
                                    value = """
                                    {
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "name": "수학 숙제 완료하기",
                                        "description": "이번 주 수학 워크북 10페이지를 모두 완료하고 검토받기",
                                        "reward": 1500,
                                        "endDate": "2024-09-07",
                                        "imageUrl": "https://example.com/homework.jpg",
                                        "label": "STUDY"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "집안일 퀘스트",
                                    description = "가사 도움 퀘스트 생성 예시",
                                    value = """
                                    {
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "name": "방 청소하기",
                                        "description": "책상 정리하고 바닥을 깨끗하게 청소하기",
                                        "reward": 800,
                                        "endDate": "2024-09-02",
                                        "imageUrl": null,
                                        "label": "HOUSEHOLD"
                                    }
                                    """
                            ),
                            @ExampleObject(
                                    name = "생활습관 퀘스트",
                                    description = "좋은 습관 형성 퀘스트 예시",
                                    value = """
                                    {
                                        "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                        "name": "일주일 일찍 일어나기",
                                        "description": "7시 30분까지 스스로 일어나서 준비하기 (7일 연속)",
                                        "reward": 2000,
                                        "endDate": "2024-09-10",
                                        "imageUrl": "https://example.com/morning.jpg",
                                        "label": "HABIT"
                                    }
                                    """
                            )
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "✅ 퀘스트 생성 성공",
                    content = @Content(schema = @Schema(implementation = QuestResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 요청 데이터 (날짜 형식 오류 등)"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (부모 권한 필요)"),
            @ApiResponse(responseCode = "403", description = "❌ 본인의 자녀가 아님"),
            @ApiResponse(responseCode = "404", description = "❌ 자녀를 찾을 수 없음")
    })
    public ResponseEntity<QuestResponse> createParentQuest(@RequestBody ParentQuestRequest request) {
        try {
            UUID parentId = getCurrentUserId();
            QuestResponse createdQuest = questService.createParentQuest(request, parentId);
            return ResponseEntity.ok(createdQuest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/parent")
    @Operation(
            summary = "부모용 퀘스트 조회",
            description = """
                    **부모가 자녀의 퀘스트 목록과 상태를 조회합니다.**
                    
                    👨‍👩‍👧‍👦 **부모 관리 기능:**
                    • 자녀가 수행 중인 모든 퀘스트 현황 확인
                    • 완료 요청된 퀘스트 승인/거부
                    • 자녀의 현재 포인트 확인
                    • 퀘스트별 진행 상황 모니터링
                    
                    📊 **조회 가능한 정보:**
                    • 퀘스트 이름, 설명, 상태
                    • 보상 포인트, 마감일
                    • 퀘스트 라벨 (카테고리)
                    • 생성일시, 진행 상황
                    
                    🔍 **필터링 옵션:**
                    • 일일퀘스트만 조회 - 매일 자동 생성되는 기본 퀘스트
                    • 부모퀘스트만 조회 - 내가 생성한 커스텀 퀘스트
                    • 전체 조회 - 모든 퀘스트
                    
                    💡 **활용 팁:**
                    • `PENDING_APPROVAL` 상태의 퀘스트는 승인이 필요함
                    • 마감일이 임박한 퀘스트 확인 가능
                    • 자녀의 관심사와 성취도 파악
                    """
    )
    @Parameter(
            name = "childId",
            description = "조회할 자녀의 UUID (필수)",
            example = "2caf849e-69d7-4136-a7ce-f58d234f1941",
            required = true
    )
    @Parameter(
            name = "type",
            description = """
                    조회할 퀘스트 타입 필터 (선택사항)
                    
                    **허용값:**
                    • `daily` - 일일퀘스트만 조회
                    • `parent` - 부모퀘스트만 조회  
                    • **미입력** - 모든 퀘스트 조회
                    """,
            example = "parent"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "✅ 퀘스트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "부모가 자녀 퀘스트 조회",
                                            description = "자녀의 모든 퀘스트와 포인트 정보",
                                            value = """
                                            {
                                                "currentPoint": 10100,
                                                "quests": [
                                                    {
                                                        "quest_id": "550e8400-e29b-41d4-a716-446655440003",
                                                        "child_id": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                                        "type": "parent",
                                                        "name": "숙제 완료하기",
                                                        "description": "이번 주 수학 숙제를 모두 완료해보자!",
                                                        "state": "PENDING_APPROVAL",
                                                        "end_date": "2025-06-20T23:59:59",
                                                        "created": "2025-06-15T10:30:00",
                                                        "isStatic": false,
                                                        "reward": 300,
                                                        "imageUrl": "https://example.com/homework.jpg",
                                                        "label": "STUDY"
                                                    },
                                                    {
                                                        "quest_id": "643dfa5d-2794-41f0-8eef-8d98054cf2df",
                                                        "child_id": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                                        "type": "daily",
                                                        "name": "양치하기",
                                                        "description": "밥 먹었으면 포포와 양치하자!",
                                                        "state": "COMPLETED",
                                                        "end_date": "2025-06-18T23:59:59",
                                                        "created": "2025-06-18T00:00:00",
                                                        "isStatic": false,
                                                        "reward": 100,
                                                        "imageUrl": null,
                                                        "label": "HABIT"
                                                    }
                                                ]
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 파라미터 (childId 누락 등)"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (부모 권한 필요)"),
            @ApiResponse(responseCode = "403", description = "❌ 다른 부모의 자녀 조회 시도"),
            @ApiResponse(responseCode = "404", description = "❌ 자녀를 찾을 수 없음")
    })
    public ResponseEntity<QuestListWithPointResponse> getQuestsForParent(
            @RequestParam UUID childId,
            @RequestParam(required = false) String type
    ) {
        try {
            QuestListWithPointResponse response = questService.getQuestsWithPoint(childId, type);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}