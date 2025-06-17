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
@Tag(name = "퀘스트 관리", description = "아이와 부모를 위한 퀘스트 생성, 조회, 상태 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class QuestController {
    private final QuestService questService;

    @GetMapping
    @Operation(
            summary = "퀘스트 목록 조회 (포인트 정보 포함)",
            description = "아이의 퀘스트 목록과 현재 포인트 정보를 함께 조회합니다. 포인트는 최상위 레벨에 한 번만 표시됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "퀘스트 및 포인트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QuestListWithPointResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "일일퀘스트 목록 + 포인트",
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
                                                        "imageUrl": null
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
                                                        "imageUrl": null
                                                    }
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "부모퀘스트 목록 + 포인트",
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
                                                        "imageUrl": "https://example.com/homework.jpg"
                                                    }
                                                ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "빈 목록 + 포인트",
                                            value = """
                                            {
                                                "currentPoint": 10100,
                                                "quests": []
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 파라미터",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<QuestListWithPointResponse> getQuests(
            @Parameter(
                    description = "퀘스트 타입 필터 (parent: 부모퀘스트, daily: 일일퀘스트)",
                    example = "daily",
                    schema = @Schema(allowableValues = {"parent", "daily"})
            )
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
            description = "퀘스트의 상태를 변경합니다. COMPLETED 상태로 변경 시 자동으로 포인트가 지급됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "상태 변경 성공",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "퀘스트 상태가 변경되었습니다.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 또는 상태 변경 규칙 위반",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = {
                                    @ExampleObject(name = "잘못된 상태", value = "유효하지 않은 상태입니다: INVALID_STATE"),
                                    @ExampleObject(name = "변경 불가", value = "COMPLETED에서 IN_PROGRESS로 변경할 수 없습니다."),
                                    @ExampleObject(name = "퀘스트 없음", value = "퀘스트를 찾을 수 없습니다.")
                            }
                    )
            )
    })
    public ResponseEntity<String> changeQuestState(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "퀘스트 상태 변경 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QuestStateChangeRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "보상 받기 (포인트 지급)",
                                            description = "아이가 보상을 받아 퀘스트를 완료할 때",
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
            @RequestBody QuestStateChangeRequest request){
        try{
            questService.changeQuestState(request);
            return ResponseEntity.ok("퀘스트 상태가 변경되었습니다.");
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

// 📁 quest/controller/QuestController.java - createParentQuest 메서드의 예시 수정

    @PostMapping("/create")
    @Operation(
            summary = "부모 퀘스트 생성",
            description = "부모가 아이에게 새로운 커스텀 퀘스트를 생성합니다. 마감날짜는 YYYY-MM-DD 형식으로 보내주시면 자동으로 해당 날짜의 23:59:59로 설정됩니다."
    )
    public ResponseEntity<QuestResponse> createParentQuest(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "부모 퀘스트 생성 요청 정보",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ParentQuestRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "부모 퀘스트 생성",
                                            value = """
                                        {
                                            "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                            "name": "해워니퀘스트",
                                            "description": "클리어하라!!!",
                                            "reward": 2000,
                                            "endDate": "2024-09-01",
                                            "imageUrl": "https://example.com/image.jpg"
                                        }
                                        """
                                    ),
                                    @ExampleObject(
                                            name = "숙제 퀘스트",
                                            value = """
                                        {
                                            "childId": "2caf849e-69d7-4136-a7ce-f58d234f1941",
                                            "name": "수학 숙제 완료하기",
                                            "description": "이번 주 수학 워크북 10페이지 완료",
                                            "reward": 1500,
                                            "endDate": "2024-09-07",
                                            "imageUrl": null
                                        }
                                        """
                                    )
                            }
                    )
            )
            @RequestBody ParentQuestRequest request) {

        try {
            UUID parentId = getCurrentUserId();
            QuestResponse createdQuest = questService.createParentQuest(request,parentId);
            return ResponseEntity.ok(createdQuest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}