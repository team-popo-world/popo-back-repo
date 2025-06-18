package com.popoworld.backend.market.dto.child;

import lombok.Data;

import java.util.UUID;

@Data
public class PurchaseItemRequest {
    private UUID productId;
    private int amount; //구매 수량
}
