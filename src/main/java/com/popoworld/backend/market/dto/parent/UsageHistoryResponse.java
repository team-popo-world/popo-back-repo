package com.popoworld.backend.market.dto.parent;

import com.popoworld.backend.market.entity.ProductUsage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageHistoryResponse { //아이 사용 내역
    private UUID usageId; //사용 내역 삭제도 할 수 있을 수 있으니 남겨놓기 지금 당장은 안쓰일듯
    private String childName; //사용한 자녀 이름
    private String productName; //상품 이름
    private int usedAmount; //사용한 수량
    private LocalDateTime usedAt; //사용 시간

    public static UsageHistoryResponse fromEntity(ProductUsage usage) {
        UsageHistoryResponse dto = new UsageHistoryResponse();
        dto.usageId = usage.getUsageId();
        dto.childName = usage.getChild().getName(); // User 엔티티에 name 필드가 있다고 가정
        dto.productName = usage.getProduct().getProductName();
        dto.usedAmount = usage.getUsedAmount();
        dto.usedAt = usage.getUsedAt();
        return dto;
    }

    }
