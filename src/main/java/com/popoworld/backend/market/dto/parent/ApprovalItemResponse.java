package com.popoworld.backend.market.dto.parent;

import com.popoworld.backend.market.entity.Product;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApprovalItemResponse {
    private UUID productId;
    private String childName;
    private String productName;
    private int price;
    private String imageUrl;
    private LocalDateTime usedAt;

    public static ApprovalItemResponse fromEntity(Product product) {
        ApprovalItemResponse dto = new ApprovalItemResponse();
        dto.productId = product.getProductId();
        dto.childName = product.getUser().getName();
        dto.productName = product.getProductName();
        dto.price = product.getProductPrice();
        dto.imageUrl = product.getProductImage();
        dto.usedAt = product.getUpdatedAt();
        return dto;
    }
}
