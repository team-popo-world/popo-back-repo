package com.popoworld.backend.market.controller.child;

import com.popoworld.backend.market.dto.child.MarketItemDTO;
import com.popoworld.backend.market.service.child.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    // 조회 API
//    @GetMapping
//    public ResponseEntity<List<MarketItemDTO>> getMarketItems(@RequestParam("type") String type) {
//        List<MarketItemDTO> items = marketService.getItemsByType(type);
//        return ResponseEntity.ok(items);
//    }

    // 구매 API
    @PostMapping("/buy")
    public ResponseEntity<String> purchaseItem() {

        return ResponseEntity.ok("구매 처리 완료");
    }

    // 사용 API

}
