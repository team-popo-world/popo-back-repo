package com.popoworld.backend.market.dto.child;

import com.popoworld.backend.market.entity.Product;
import lombok.Data;

import java.util.UUID;

@Data
public class MarketItemDTO {
    private UUID id;
    private String name;
    private int price;
    private int quantity; // 무한일 경우 -1 또는 Integer.MAX_VALUE로 처리
    private String type;

//    public static MarketItemDTO fromEntity(Product product) {
//        MarketItemDTO dto = new MarketItemDTO();
//        dto.id = product.getProductId();
//        dto.name = product.getProductName();
//        dto.price = product.getProductPrice();
//        dto.quantity = product.getProductStock();
//        return dto;
//    }
}
