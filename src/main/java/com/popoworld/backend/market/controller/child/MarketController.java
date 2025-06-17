package com.popoworld.backend.market.controller.child;

import com.popoworld.backend.market.dto.child.MarketItemResponse;
import com.popoworld.backend.market.service.child.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/market")
@Tag(name = "상점", description = "상점 관련 API")
public class MarketController {

    private final MarketService marketService;

    // 조회 API
    @GetMapping
    @Operation(summary = "상점 아이템 조회", description = "타입에 따라 NPC 상점 또는 부모 상점 아이템을 조회합니다")
    @Parameter(name = "type", description = "상점 타입 (npc: NPC상점, parent: 부모상점)", required = true)
    public ResponseEntity<List<MarketItemResponse>> getMarketItems(@RequestParam("type") String type) {
        List<MarketItemResponse> items = marketService.getItemsByType(type);
        return ResponseEntity.ok(items);
    }

    // 구매 API
    @PostMapping("/buy")
    public ResponseEntity<String> purchaseItem() {

        return ResponseEntity.ok("구매 처리 완료");
    }

    // 사용 API

}
