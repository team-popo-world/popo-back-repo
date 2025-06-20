package com.popoworld.backend.market.dto.child;

import lombok.Data;

import java.util.UUID;

@Data
public class PurchaseItemRequest {
    private UUID productId;
    private Integer amount; //NPC 상품 용, 부모 상품은 항상 1개
}
