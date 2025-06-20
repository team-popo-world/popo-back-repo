package com.popoworld.backend.market.dto.child;

import lombok.Data;

import java.util.UUID;

@Data
public class UseItemRequest {
    private UUID productId;
    private Integer amount; //NPC 상품용. 부모 상품은 1개 무조건
    }
