package com.popoworld.backend.market.dto.child;

import com.popoworld.backend.market.entity.Inventory;
import lombok.Data;

import java.util.UUID;

@Data
public class InventoryItemResponse {
    private UUID productId;
    private String name;
    private String imageUrl;
    private int stock; //보유 수량
    private String type; //"npc" or "parent"
    private int exp;

    public static InventoryItemResponse fromEntity(Inventory inventory){
        InventoryItemResponse dto = new InventoryItemResponse();
        dto.productId = inventory.getProduct().getProductId();
        dto.name = inventory.getProduct().getProductName();
        dto.imageUrl=inventory.getProduct().getProductImage();
        dto.stock=inventory.getStock();

        //타입에 따른 설정
        // 타입에 따른 설정
        if (inventory.getProduct().getUser() == null) {
            dto.type = "npc";
            dto.exp = inventory.getProduct().getExp();
        } else {
            dto.type = "parent";
            dto.exp = 0;
        }

        return dto;
    }
}
