package com.popoworld.backend.market.dto.parent;

import com.popoworld.backend.market.entity.Product;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApprovalItemResponse {
    private UUID productId;
    private String childName;
    private String productName;
    private int price;
    private String imageUrl;
    private LocalDate usedAt;

    public static ApprovalItemResponse fromEntity(Product product) {
        ApprovalItemResponse dto = new ApprovalItemResponse();
        dto.productId = product.getProductId();
        dto.childName = product.getUser().getName();
        dto.productName = product.getProductName();
        dto.price = product.getProductPrice();
        dto.imageUrl = product.getProductImage();
        if (product.getUpdatedAt() != null) {
            dto.usedAt = product.getUpdatedAt().toLocalDate();
        }

        return dto;
    }
}
