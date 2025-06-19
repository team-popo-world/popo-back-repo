// MarketController.java
package com.popoworld.backend.market.controller;

import com.popoworld.backend.market.dto.child.*;
import com.popoworld.backend.market.dto.parent.ApprovalItemResponse;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.service.child.InventoryService;
import com.popoworld.backend.market.service.child.MarketService;
import com.popoworld.backend.market.service.parent.MarketParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
// 👇 클래스 레벨 태그 제거
public class MarketController {

    private final MarketService marketService;
    private final MarketParentService marketParentService;
    private final InventoryService inventoryService;

    // ===== 공통 API =====

    @GetMapping
    @Tag(name = "시장 자녀용 API") // 👈 수정됨
    @Operation(
            summary = "상점 아이템 조회",
            description = """
                    상점 타입에 따라 아이템을 조회합니다.
                    
                    **NPC 상점 (type: "npc")**
                    - 포포 키우기용 먹이 아이템들 (무한재고)
                    - 상태: 항상 REGISTERED
                    
                    **부모 상점 (type: "parent")**  
                    - 부모가 등록한 개인화 상품들 (재고 1개)
                    - 상태: REGISTERED → PURCHASED → USED → APPROVED
                    """
    )
    @Parameter(name = "type", description = "상점 타입 ('npc' 또는 'parent')", required = true, example = "npc")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 상품 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 타입 파라미터"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<MarketItemResponse>> getMarketItems(@RequestParam("type") String type) {
        List<MarketItemResponse> items = marketService.getItemsByType(type);
        return ResponseEntity.ok(items);
    }

    // ===== 자녀용 API =====

    @PostMapping("/buy")
    @Tag(name = "시장 자녀용 API") // 👈 수정됨
    @Operation(
            summary = "상품 구매",
            description = """
                    NPC 상품 또는 부모 상품을 포인트로 구매합니다.
                    
                    **NPC 상품**: amount 파라미터로 수량 지정 (여러 개 구매 가능)
                    **부모 상품**: 무조건 1개만 구매 (amount 무시)
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "구매 요청 정보",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "NPC 상품", value = """
                                    {"productId": "550e8400-e29b-41d4-a716-446655440000", "amount": 5}"""),
                            @ExampleObject(name = "부모 상품", value = """
                                    {"productId": "550e8400-e29b-41d4-a716-446655440000"}""")
                    }
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 구매 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 포인트 부족 또는 재고 부족"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "404", description = "❌ 상품을 찾을 수 없음")
    })
    public ResponseEntity<PurchaseItemResponse> purchaseItem(@RequestBody PurchaseItemRequest request) {
        UUID childId = getCurrentUserId();
        log.info("🛒 구매 요청: 상품ID={}, 수량={}, 사용자ID={}", request.getProductId(), request.getAmount(), childId);

        PurchaseItemResponse response = marketService.purchaseProduct(request, childId);
        log.info("🎉 구매 완료!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/inventory")
    @Tag(name = "시장 자녀용 API") // 👈 수정됨
    @Operation(
            summary = "인벤토리 조회",
            description = """
                    사용자가 보유한 모든 아이템을 조회합니다.
                    
                    **NPC 아이템**: 수량과 함께 표시, 포포 키우기에서 사용
                    **부모 아이템**: 개별 아이템으로 표시, 인벤토리에서 직접 사용
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 인벤토리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<InventoryItemResponse>> getUserInventory() {
        UUID userId = getCurrentUserId();
        List<InventoryItemResponse> inventory = inventoryService.getUserInventory(userId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/inventory/use")
    @Tag(name = "시장 자녀용 API") // 👈 수정됨
    @Operation(
            summary = "부모 상품 사용",
            description = """
                    인벤토리에서 부모 상품을 사용합니다.
                    
                    **부모 상품만 사용 가능**: NPC 상품은 포포 키우기에서만 사용
                    **사용 효과**: 상품 상태가 USED로 변경되어 부모 승인 대기
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "상품 사용 요청",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "부모 상품 사용", value = """
                            {"productId": "550e8400-e29b-41d4-a716-446655440000"}""")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 부모 상품 사용 성공"),
            @ApiResponse(responseCode = "400", description = "❌ NPC 상품은 사용 불가"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "404", description = "❌ 보유하지 않은 상품")
    })
    public ResponseEntity<UseItemResponse> useItem(@RequestBody UseItemRequest request) {
        UUID childId = getCurrentUserId();
        UseItemResponse response = inventoryService.useItem(request, childId);
        return ResponseEntity.ok(response);
    }

    // ===== 부모용 API =====

    @PostMapping("/parent/products")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "부모 상품 등록",
            description = """
                    부모가 자녀를 위한 상품을 등록합니다.
                    
                    **등록 규칙**: 재고는 항상 1개, 상태는 REGISTERED
                    **라벨 종류**: SNACK, ENTERTAINMENT, TOY, EDUCATION, ETC
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "상품 등록 정보",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "childId": "550e8400-e29b-41d4-a716-446655440000",
                                "productName": "레고 클래식 세트",
                                "productPrice": 50000,
                                "productImage": "https://example.com/lego.jpg",
                                "label": "TOY"
                            }""")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "✅ 상품 등록 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "403", description = "❌ 본인의 자녀가 아님")
    })
    public ResponseEntity<MarketItemResponse> createParentProduct(@RequestBody CreateProductRequest request) {
        UUID parentId = getCurrentUserId();
        log.info("부모 상품 등록 요청: {}", request);

        MarketItemResponse createdProduct = marketParentService.createParentProduct(request, parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @GetMapping("/parent/products")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "내가 등록한 상품 목록",
            description = "부모가 등록한 상품 목록을 조회합니다. (REGISTERED 상태만 표시)"
    )
    @Parameter(name = "childId", description = "특정 자녀의 상품만 조회 (선택사항)", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 등록 상품 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<MarketItemResponse>> getMyProducts(
            @RequestParam(value = "childId", required = false) UUID childId) {
        UUID parentId = getCurrentUserId();
        List<MarketItemResponse> products = marketParentService.getMyProducts(parentId, childId);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/parent/products/{productId}")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "부모 상품 삭제",
            description = """
                    부모가 등록한 상품을 삭제합니다.
                    
                    **삭제 조건**: REGISTERED 상태인 상품만 삭제 가능 (구매 전)
                    **처리 방식**: 상태를 DISCONTINUED로 변경
                    """
    )
    @Parameter(name = "productId", description = "삭제할 상품의 UUID", required = true)
    @Parameter(name = "childId", description = "해당 자녀의 UUID", required = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 상품 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 이미 구매된 상품"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "403", description = "❌ 권한 없음"),
            @ApiResponse(responseCode = "404", description = "❌ 상품을 찾을 수 없음")
    })
    public ResponseEntity<String> deleteParentProduct(
            @PathVariable UUID productId,
            @RequestParam UUID childId) {
        UUID parentId = getCurrentUserId();
        log.info("상품 삭제 요청: 상품ID={}, 자녀ID={}, 부모ID={}", productId, childId, parentId);

        marketParentService.deleteParentProduct(productId, childId, parentId);
        return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
    }

    @GetMapping("/parent/pending-approvals")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "자녀 상품 사용 승인 대기 목록",
            description = """
                    자녀가 사용한 상품 중 부모 승인이 필요한 목록을 조회합니다.
                    
                    **조회 상태**: USED (자녀가 사용했지만 아직 승인되지 않음)
                    **정렬**: 사용 시간순 (최신순)
                    """
    )
    @Parameter(name = "childId", description = "조회할 자녀의 UUID (선택사항)", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 승인 대기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "403", description = "❌ 권한 없음")
    })
    public ResponseEntity<List<ApprovalItemResponse>> getPendingApprovals(
            @RequestParam(value = "childId", required = false) UUID childId) {
        UUID parentId = getCurrentUserId();
        List<ApprovalItemResponse> pendingApprovals = marketParentService.getPendingApprovals(parentId, childId);
        return ResponseEntity.ok(pendingApprovals);
    }

    @PostMapping("/parent/approve/{productId}")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "자녀 상품 사용 승인",
            description = """
                    자녀가 사용 요청한 상품을 승인합니다.
                    
                    **승인 프로세스**: USED → APPROVED 상태 변경
                    **안전성**: childId로 정확한 자녀의 상품인지 확인
                    """
    )
    @Parameter(name = "productId", description = "승인할 상품의 UUID", required = true)
    @Parameter(name = "childId", description = "해당 자녀의 UUID", required = true)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 상품 사용 승인 성공"),
            @ApiResponse(responseCode = "400", description = "❌ 승인 대기 상태가 아님"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패"),
            @ApiResponse(responseCode = "403", description = "❌ 권한 없음"),
            @ApiResponse(responseCode = "404", description = "❌ 상품을 찾을 수 없음")
    })
    public ResponseEntity<String> approveUsage(
            @PathVariable UUID productId,
            @RequestParam UUID childId) {
        UUID parentId = getCurrentUserId();
        log.info("사용 승인 요청: 상품ID={}, 자녀ID={}, 부모ID={}", productId, childId, parentId);

        marketParentService.approveUsage(productId, childId, parentId);
        return ResponseEntity.ok("상품 사용이 승인되었습니다.");
    }

    @GetMapping("/parent/approved-history")
    @Tag(name = "시장 부모용 API") // 👈 수정됨
    @Operation(
            summary = "승인 완료된 상품 내역 조회",
            description = """
                    부모가 승인 완료한 자녀의 상품 사용 내역을 조회합니다.
                    
                    **조회 상태**: APPROVED (승인 완료된 상품들)
                    **정렬**: 승인 시간순 (최신순)
                    """
    )
    @Parameter(name = "childId", description = "조회할 자녀의 UUID (선택사항)", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "✅ 승인 완료 내역 조회 성공"),
            @ApiResponse(responseCode = "401", description = "❌ 인증 실패")
    })
    public ResponseEntity<List<ApprovalItemResponse>> getApprovedHistory(
            @RequestParam(value = "childId", required = false) UUID childId) {
        UUID parentId = getCurrentUserId();
        List<ApprovalItemResponse> history = marketParentService.getApprovedHistory(parentId, childId);
        return ResponseEntity.ok(history);
    }
}