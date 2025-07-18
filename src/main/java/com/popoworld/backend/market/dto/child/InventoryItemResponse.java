package com.popoworld.backend.market.dto.child;

import com.popoworld.backend.market.entity.Inventory;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class InventoryItemResponse {
    private UUID productId;
    private String name;
    private String imageUrl;
    private int stock; //보유 수량. NPC 상품: 실제 수량, 부모 상품: 항상 1
    private String type; //"npc" or "parent"
    private int exp;
    private int price;
    private LocalDate purchasedAt;

    public static InventoryItemResponse fromEntity(Inventory inventory){
        InventoryItemResponse dto = new InventoryItemResponse();
        dto.productId = inventory.getProduct().getProductId();
        dto.name = inventory.getProduct().getProductName();
        dto.imageUrl=inventory.getProduct().getProductImage();
        dto.stock=inventory.getStock();
        dto.price = inventory.getProduct().getProductPrice();
        dto.purchasedAt = inventory.getPurchasedAt().toLocalDate();


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
