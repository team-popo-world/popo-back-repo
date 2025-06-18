package com.popoworld.backend.market.dto.child;

import lombok.Data;

import java.util.UUID;

@Data
public class UseItemRequest {
    private UUID productId;
    private int amount; //사용할 수량
    }
