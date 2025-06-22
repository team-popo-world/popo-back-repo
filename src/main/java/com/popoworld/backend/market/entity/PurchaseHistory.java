package com.popoworld.backend.market.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "purchase_history")
public class PurchaseHistory {
    @Id
    @Field(targetType = FieldType.STRING)
    private String id;
    private String type; //npc, parent
    private String name;        // 상품명
    private Integer price;      // 상품 가격
    private Integer cnt;        // 구매 수량
    private LocalDateTime timestamp;

    @Field(targetType = FieldType.STRING)
    private String childId; //사용자 구분    // 자녀 ID
    @Field(targetType = FieldType.STRING)
    private String productId;     // 상품 ID
    @Field(targetType = FieldType.STRING)
    private String parentId;      // 부모 ID (부모 상품인 경우)
    private ProductLabel label; // ML 분석용 라벨
}
