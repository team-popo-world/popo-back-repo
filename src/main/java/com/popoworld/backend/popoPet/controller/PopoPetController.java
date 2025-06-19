package com.popoworld.backend.popoPet.controller;

import com.popoworld.backend.popoPet.dto.FeedingRequest;
import com.popoworld.backend.popoPet.dto.FeedingResponse;
import com.popoworld.backend.popoPet.dto.PopoFeedResponse;
import com.popoworld.backend.popoPet.service.PopoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/popo")
@RequiredArgsConstructor
@Tag(name = "포포 키우기 API", description = "포포 펫을 키우는 먹이주기 시스템")

public class PopoPetController {
        private final PopoService popoService;

        @GetMapping("/feeds")
        @Operation(
                summary = "포포 먹이 목록 조회",
                description = """
                    **포포에게 줄 수 있는 먹이 목록과 포포 상태를 조회합니다.**
                    
                    🍎 **먹이 아이템:**
                    • NPC 상점에서 구매한 먹이만 표시 (당근, 물고기, 빵, 사과, 수박, 브로콜리)
                    • 인벤토리에 보유한 먹이만 조회됨
                    • 각 먹이마다 경험치가 다름 (8~20 경험치)
                    
                    🐣 **포포 정보:**
                    • 현재 레벨과 경험치 확인
                    • 100 경험치마다 레벨업
                    • 포포가 없으면 자동으로 생성 (레벨 1)
                    
                    💡 **팁:**
                    • 먹이는 마켓의 NPC 상점에서 포인트로 구매
                    • 다양한 먹이를 주면 더 많은 경험치 획득
                    • 레벨이 올라갈수록 포포가 더 예뻐져요!
                    """
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "✅ 먹이 목록 조회 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = PopoFeedResponse.class),
                                examples = @ExampleObject(
                                        name = "먹이 목록 조회 응답",
                                        value = """
                                    {
                                        "currentLevel": 3,
                                        "currentExperience": 45,
                                        "totalExperience": 245,
                                        "availableFeeds": [
                                            {
                                                "productId": "fa7ea10c-569c-451d-ac2c-054d3ca736fe",
                                                "name": "당근",
                                                "imageUrl": "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/carrot_i3xbjj",
                                                "stock": 3,
                                                "type": "npc",
                                                "exp": 10
                                            },
                                            {
                                                "productId": "550e8400-e29b-41d4-a716-446655440001",
                                                "name": "물고기",
                                                "imageUrl": "https://res.cloudinary.com/djmcg7zgu/image/upload/w_auto,f_auto,q_auto/v1749382424/fish_hqsqfs",
                                                "stock": 1,
                                                "type": "npc",
                                                "exp": 15
                                            }
                                        ]
                                    }
                                    """
                                )
                        )
                ),
                @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
        })
        public ResponseEntity<PopoFeedResponse> getAvailableFeeds() {
            UUID userId = getCurrentUserId();
            PopoFeedResponse response = popoService.getAvailableFeeds(userId);
            return ResponseEntity.ok(response);
        }

        @PostMapping("/feed")
        @Operation(
                summary = "포포에게 먹이주기",
                description = """
                    **포포에게 먹이를 줘서 경험치를 올리고 레벨업시킵니다.**
                    
                    🍽️ **먹이주기 규칙:**
                    • 한 번에 여러 종류의 먹이를 줄 수 있음
                    • 같은 종류 먹이는 1개씩만 가능 (amount는 항상 1)
                    • NPC 먹이만 사용 가능 (부모 상품 불가)
                    
                    ⚡ **처리 과정:**
                    1. 인벤토리에서 먹이 보유 여부 확인
                    2. 인벤토리에서 먹이 차감
                    3. 포포에게 경험치 추가
                    4. 100 경험치마다 자동 레벨업
                    
                    🎉 **레벨업 시스템:**
                    • 100 경험치 = 1레벨
                    • 레벨업시 특별 메시지 표시
                    • 총 누적 경험치도 함께 기록
                    
                    ⚠️ **주의사항:**
                    • 먹이가 부족하면 전체 요청 실패
                    • 부모 상품을 먹이로 줄 수 없음
                    • 한 번에 같은 먹이 여러 개 불가
                    """
        )
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "포포 먹이주기 요청",
                required = true,
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = FeedingRequest.class),
                        examples = {
                                @ExampleObject(
                                        name = "한 종류 먹이",
                                        description = "당근 1개만 주기",
                                        value = """
                                    {
                                        "feedItems": [
                                            {
                                                "productId": "fa7ea10c-569c-451d-ac2c-054d3ca736fe",
                                                "amount": 1
                                            }
                                        ]
                                    }
                                    """
                                ),
                                @ExampleObject(
                                        name = "여러 종류 먹이",
                                        description = "당근, 물고기, 사과 각 1개씩 주기",
                                        value = """
                                    {
                                        "feedItems": [
                                            {
                                                "productId": "fa7ea10c-569c-451d-ac2c-054d3ca736fe",
                                                "amount": 1
                                            },
                                            {
                                                "productId": "550e8400-e29b-41d4-a716-446655440001",
                                                "amount": 1
                                            },
                                            {
                                                "productId": "550e8400-e29b-41d4-a716-446655440002",
                                                "amount": 1
                                            }
                                        ]
                                    }
                                    """
                                )
                        }
                )
        )
        @ApiResponses({
                @ApiResponse(
                        responseCode = "200",
                        description = "✅ 먹이주기 성공",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = FeedingResponse.class),
                                examples = {
                                        @ExampleObject(
                                                name = "일반 먹이주기",
                                                description = "레벨업 없이 경험치만 증가",
                                                value = """
                                            {
                                                "newLevel": 3,
                                                "currentExperience": 65,
                                                "totalExperience": 265,
                                                "gainedExperience": 20,
                                                "levelUp": false,
                                                "fedItems": ["당근", "물고기"],
                                                "message": "🍎 포포가 맛있게 먹었어요!"
                                            }
                                            """
                                        ),
                                        @ExampleObject(
                                                name = "레벨업 성공",
                                                description = "먹이주기로 레벨업한 경우",
                                                value = """
                                            {
                                                "newLevel": 4,
                                                "currentExperience": 15,
                                                "totalExperience": 315,
                                                "gainedExperience": 50,
                                                "levelUp": true,
                                                "fedItems": ["수박", "브로콜리"],
                                                "message": "🎉 포포가 레벨 4로 성장했어요!"
                                            }
                                            """
                                        )
                                }
                        )
                ),
                @ApiResponse(responseCode = "400", description = "❌ 잘못된 요청 (먹이 부족, 부모 상품 등)"),
                @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
                @ApiResponse(responseCode = "404", description = "❌ 먹이를 찾을 수 없음")
        })
        public ResponseEntity<FeedingResponse> feedPopo(@RequestBody FeedingRequest request) {
            try {
                UUID userId = getCurrentUserId();
                FeedingResponse response = popoService.feedPopo(userId, request);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            } catch (Exception e) {
                return ResponseEntity.internalServerError().build();
            }
        }
    }

