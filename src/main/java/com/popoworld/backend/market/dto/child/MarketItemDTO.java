package com.popoworld.backend.market.dto.child;

import lombok.Data;

@Data
public class MarketItemDTO {
    private Long id;
    private String name;
    private int price;
    private int quantity; // 무한일 경우 -1 또는 Integer.MAX_VALUE로 처리
    private String type;
}
