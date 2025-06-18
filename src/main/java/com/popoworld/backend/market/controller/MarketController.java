package com.popoworld.backend.market.controller;

import com.popoworld.backend.market.dto.child.InventoryItemResponse;
import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.dto.child.PurchaseItemRequest;
import com.popoworld.backend.market.dto.child.PurchaseItemResponse;
import com.popoworld.backend.market.dto.parent.CreateProductRequest;
import com.popoworld.backend.market.service.child.InventoryService;
import com.popoworld.backend.market.service.child.MarketService;
import com.popoworld.backend.market.service.parent.MarketParentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "시장", description = "시장 관련 API")
public class MarketController {

    private final MarketService marketService;
    private final MarketParentService marketParentService;
    private final InventoryService inventoryService;
        //상품 조회 API
    @GetMapping
    @Operation(summary = "상점 아이템 조회", description = "타입에 따라 NPC 상점 또는 부모 상점 아이템을 조회합니다")
    @Parameter(name = "type", description = "상점 타입 (npc: NPC상점, parent: 부모상점)", required = true)
    public ResponseEntity<List<MarketItemResponse>> getMarketItems(@RequestParam("type") String type) {
        List<MarketItemResponse> items = marketService.getItemsByType(type);
        return ResponseEntity.ok(items);
    }

    //부모 상품 등록 API
    @PostMapping("/parent/products")
    @Operation(
            summary = "부모 상품 등록",
            description = "부모가 자녀를 위한 상품을 등록합니다. 등록된 상품은 해당 자녀만 구매할 수 있습니다."
            // tags 제거
    )
    public ResponseEntity<MarketItemResponse>creaetParentProduct(@RequestBody CreateProductRequest request){
        UUID parentId = getCurrentUserId();
        log.info("부모 상품 등록 요청: {}", request);

        MarketItemResponse createdProduct = marketParentService.createParentProduct(request,parentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        //201 Created 상태 코드를 반환한다. 새로운 리소스가 성공적으로 만들어졌을 때 사용
    }

  //구매 API
    @PostMapping("/buy")
    @Operation(
            summary = "상품 구매",
            description = "NPC 상품 또는 부모가 등록한 상품을 구매합니다. " +
                    "구매 시 포인트가 차감되고 인벤토리에 아이템이 추가됩니다."
    )
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
            description = "사용자가 보유한 모든 아이템을 조회합니다. " +
                    "NPC 아이템(type: npc)은 포포 키우기에서 사용하고, " +
                    "부모 아이템(type: parent)은 인벤토리에서 직접 사용할 수 있습니다."
    )
    public ResponseEntity<List<InventoryItemResponse>> getUserInventory() {
        UUID userId = getCurrentUserId();
        List<InventoryItemResponse> inventory = inventoryService.getUserInventory(userId);
        return ResponseEntity.ok(inventory);
    }
    // 사용 API

}
