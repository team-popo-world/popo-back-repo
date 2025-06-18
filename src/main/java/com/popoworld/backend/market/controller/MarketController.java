package com.popoworld.backend.market.controller;

import com.popoworld.backend.market.dto.child.*;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.dto.parent.UsageHistoryResponse;
import com.popoworld.backend.market.service.child.InventoryService;
import com.popoworld.backend.market.service.child.MarketService;
import com.popoworld.backend.market.service.parent.MarketParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/store")
@Slf4j
@Tag(name = "시장 API", description = "포포월드 마켓플레이스 관련 API - NPC 상점과 부모 상점 관리")
public class MarketController {

    private final MarketService marketService;
    private final MarketParentService marketParentService;
    private final InventoryService inventoryService;

    // 상품 조회 API
    @GetMapping
    @Operation(
            summary = "상점 아이템 조회",
            description = """
                    **상점 타입에 따라 아이템을 조회합니다.**
                    
                    • **NPC 상점**: 포포 키우기용 먹이 아이템들 (당근, 물고기, 빵 등)
                    • **부모 상점**: 부모가 해당 자녀를 위해 등록한 실제 상품들
                    
                    ⚠️ **부모 상점은 로그인한 자녀만 볼 수 있는 개인화된 상품들입니다.**
                    """
    )
    @Parameter(
            name = "type",
            description = """
                    상점 타입을 지정합니다.
                    
                    **허용값:**
                    • `npc` - NPC 상점 (포포 먹이)
                    • `parent` - 부모 상점 (개인화 상품)
                    """,
            required = true,
            example = "npc"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 상품 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 타입 파라미터"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<MarketItemResponse>> getMarketItems(@RequestParam("type") String type) {
        List<MarketItemResponse> items = marketService.getItemsByType(type);
        return ResponseEntity.ok(items);
    }

    // 부모 상품 등록 API
    @PostMapping("/parent/products")
    @Operation(
            summary = "부모 상품 등록",
            description = """
                    **부모가 자녀를 위한 상품을 등록합니다.**
                    
                    📝 **등록 프로세스:**
                    1. 부모가 상품 정보와 라벨을 선택하여 등록
                    2. 해당 자녀만 구매할 수 있는 개인화된 상품으로 생성
                    3. 재고 관리 및 포인트 차감 시스템 적용
                    
                    🏷️ **상품 라벨 종류:**
                    • `FOOD` - 먹이 (NPC 상품 전용)
                    • `SNACK` - 간식
                    • `ENTERTAINMENT` - 오락
                    • `TOY` - 장난감  
                    • `EDUCATION` - 교육 및 문구
                    • `ETC` - 기타
                    
                    💡 **팁:** ML 분석을 위해 적절한 라벨을 선택해주세요!
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "상품 등록 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateProductRequest.class),
                    examples = @ExampleObject(
                            name = "상품 등록 예시",
                            value = """
                                    {
                                        "childId": "550e8400-e29b-41d4-a716-446655440000",
                                        "productName": "레고 클래식 세트",
                                        "productPrice": 50000,
                                        "productStock": 1,
                                        "productImage": "https://example.com/lego.jpg",
                                        "label": "TOY"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "✅ 상품 등록 성공",
                    content = @Content(schema = @Schema(implementation = MarketItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (부모 권한 필요)"),
            @ApiResponse(responseCode = "403", description = "❌ 본인의 자녀가 아님")
    })
    public ResponseEntity<MarketItemResponse> createParentProduct(@RequestBody CreateProductRequest request) {
        UUID parentId = getCurrentUserId();
        log.info("부모 상품 등록 요청: {}", request);

        MarketItemResponse createdProduct = marketParentService.createParentProduct(request, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    // 구매 API
    @PostMapping("/buy")
    @Operation(
            summary = "상품 구매",
            description = """
                    **NPC 상품 또는 부모 상품을 포인트로 구매합니다.**
                    
                    🛒 **구매 프로세스:**
                    1. 포인트 잔액 확인
                    2. 재고 확인 (부모 상품만, NPC 상품은 무한재고)
                    3. 포인트 차감 및 재고 차감
                    4. 인벤토리에 아이템 추가
                    5. 구매 이력 MongoDB에 저장 (ML 분석용)
                    
                    💰 **포인트 시스템:**
                    • 퀘스트 완료, 출석 체크 등으로 포인트 획득
                    • 1포인트 = 1원 단위로 계산
                    
                    📦 **재고 관리:**
                    • NPC 상품: 무한재고 (-1)
                    • 부모 상품: 설정된 재고량만큼 구매 가능
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "구매 요청 정보",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PurchaseItemRequest.class),
                    examples = @ExampleObject(
                            name = "구매 요청 예시",
                            value = """
                                    {
                                        "productId": "550e8400-e29b-41d4-a716-446655440000",
                                        "amount": 2
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 구매 성공",
                    content = @Content(schema = @Schema(implementation = PurchaseItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ 포인트 부족 또는 재고 부족"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "404", description = "❌ 상품을 찾을 수 없음")
    })
    public ResponseEntity<PurchaseItemResponse> purchaseItem(@RequestBody PurchaseItemRequest request) {
        System.out.println("=== 🛒 구매 API 진입! ===");
        System.out.println("Request: " + request.getProductId() + ", amount: " + request.getAmount());

        UUID childId = getCurrentUserId();
        System.out.println("User ID: " + childId);

        PurchaseItemResponse response = marketService.purchaseProduct(request, childId);
        System.out.println("=== 🎉 구매 완료! ===");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    @Operation(
            summary = "인벤토리 조회",
            description = """
                    **사용자가 보유한 모든 아이템을 조회합니다.**
                    
                    📦 **아이템 타입별 사용법:**
                    • **NPC 아이템** (`type: "npc"`): 포포 키우기에서 사용
                      - 당근, 물고기, 빵 등의 먹이 아이템
                      - 사용시 포포 경험치 증가
                    
                    • **부모 아이템** (`type: "parent"`): 인벤토리에서 직접 사용
                      - 실제 상품 (장난감, 간식, 교육용품 등)
                      - 사용시 부모에게 알림 전송
                      - 사용 내역 추적
                    
                    💡 **재고 관리:** 수량이 0인 아이템은 자동으로 필터링됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 인벤토리 조회 성공",
                    content = @Content(schema = @Schema(implementation = InventoryItemResponse.class))),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<InventoryItemResponse>> getUserInventory() {
        UUID userId = getCurrentUserId();
        List<InventoryItemResponse> inventory = inventoryService.getUserInventory(userId);
        return ResponseEntity.ok(inventory);
    }

    // 사용 API - 자녀용
    @PostMapping("/inventory/usage")
    @Operation(
            summary = "인벤토리 상품 사용",
            description = """
                    **부모가 등록한 상품을 인벤토리에서 사용합니다.**
                    
                    ⚠️ **주의사항:**
                    • NPC 상품은 사용 불가 (포포 키우기에서만 사용)
                    • 부모 상품만 인벤토리에서 직접 사용 가능
                    
                    📬 **알림 시스템:**
                    1. 상품 사용시 인벤토리에서 수량 차감
                    2. 사용 내역이 DB에 기록
                    3. 부모에게 실시간 알림 전송
                    
                    📊 **추적 정보:**
                    • 사용 시간, 수량, 상품명 등이 기록
                    • 부모는 사용 내역 조회 API로 확인 가능
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "상품 사용 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UseItemRequest.class),
                    examples = @ExampleObject(
                            name = "상품 사용 예시",
                            value = """
                                    {
                                        "productId": "550e8400-e29b-41d4-a716-446655440000",
                                        "amount": 1
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 상품 사용 성공",
                    content = @Content(schema = @Schema(implementation = UseItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "❌ NPC 상품은 사용 불가 또는 수량 부족"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "404", description = "❌ 보유하지 않은 상품")
    })
    public ResponseEntity<UseItemResponse> useItem(@RequestBody UseItemRequest request) {
        UUID childId = getCurrentUserId();
        UseItemResponse response = inventoryService.useItem(request, childId);
        return ResponseEntity.ok(response);
    }

    // 사용 내역 조회 - 부모용
    @GetMapping("/parent/usage-history")
    @Operation(
            summary = "자녀 상품 사용 내역 조회",
            description = """
                    **부모가 등록한 상품을 자녀가 사용한 내역을 시간순으로 조회합니다.**
                    
                    🔍 **조회 옵션:**
                    • **특정 자녀**: `childId` 파라미터로 특정 자녀의 사용 내역만 조회
                    • **모든 자녀**: `childId` 미입력시 등록된 모든 자녀의 사용 내역 조회
                    
                    📋 **포함 정보:**
                    • 자녀 이름, 상품명, 사용 수량, 사용 시간
                    • 시간 역순 정렬 (최신 사용 내역이 먼저)
                    
                    💡 **활용 팁:**
                    • 자녀의 상품 소비 패턴 파악
                    • 약속한 상품 사용 규칙 준수 여부 확인
                    • 가정 내 보상 시스템 관리
                    """
    )
    @Parameter(
            name = "childId",
            description = """
                    조회할 자녀의 UUID입니다.
                    
                    • **입력시**: 해당 자녀의 사용 내역만 조회
                    • **미입력시**: 모든 자녀의 사용 내역 조회
                    """,
            required = false,
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 사용 내역 조회 성공",
                    content = @Content(schema = @Schema(implementation = UsageHistoryResponse.class))),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (부모 권한 필요)"),
            @ApiResponse(responseCode = "403", description = "❌ 다른 부모의 자녀 내역 접근 시도")
    })
    public ResponseEntity<List<UsageHistoryResponse>> getUsageHistory(
            @RequestParam(value = "childId", required = false) UUID childId) {
        UUID parentId = getCurrentUserId();
        List<UsageHistoryResponse> history = marketParentService.getUsageHistory(parentId, childId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/parent/products")
    @Operation(
            summary = "내가 등록한 상품 목록",
            description = """
                    **부모가 등록한 상품 목록을 조회합니다.**
                    
                    🔍 **조회 옵션:**
                    • **특정 자녀용**: `childId` 파라미터로 특정 자녀를 위해 등록한 상품만 조회
                    • **모든 상품**: `childId` 미입력시 모든 자녀용으로 등록한 상품 조회
                    
                    📦 **상품 상태 정보:**
                    • `quantity`: 남은 재고 수량 (-1은 무한재고)
                    • `type`: "parent" (부모 등록 상품)
                    • `label`: 상품 카테고리 (ML 분석용)
                    
                    💼 **관리 기능:**
                    • 등록한 상품의 구매 현황 파악
                    • 재고 관리 및 추가 등록 결정
                    • 자녀별 맞춤 상품 현황 확인
                    """
    )
    @Parameter(
            name = "childId",
            description = """
                    조회할 자녀의 UUID입니다.
                    
                    • **입력시**: 해당 자녀용으로 등록한 상품만 조회
                    • **미입력시**: 모든 자녀용 등록 상품 조회
                    """,
            required = false,
            example = "550e8400-e29b-41d4-a716-446655440000"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 등록 상품 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = MarketItemResponse.class))),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패 (부모 권한 필요)")
    })
    public ResponseEntity<List<MarketItemResponse>> getMyProducts(
            @RequestParam(value = "childId", required = false) UUID childId) {
        UUID parentId = getCurrentUserId();
        List<MarketItemResponse> products = marketParentService.getMyProducts(parentId, childId);
        return ResponseEntity.ok(products);
    }
}