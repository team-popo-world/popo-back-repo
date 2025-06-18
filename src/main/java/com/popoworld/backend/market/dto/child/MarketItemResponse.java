package com.popoworld.backend.market.dto.child;

import com.popoworld.backend.market.entity.Product;
import lombok.Data;

import java.util.UUID;

@Data
public class MarketItemResponse {
    private UUID id;
    private String name;
    private int price;
    private int quantity; // 무한일 경우 -1 또는 Integer.MAX_VALUE로 처리
    private String type;
    private String imageUrl;

    public static MarketItemResponse fromEntity(Product product) {
        MarketItemResponse dto = new MarketItemResponse();
        dto.id = product.getProductId();
        dto.name = product.getProductName();
        dto.price = product.getProductPrice();
        dto.quantity = product.getProductStock();
        dto.imageUrl = product.getProductImage();

        dto.type=(product.getUser()==null) ? "npc":"parent";
        return dto;
    }
}
