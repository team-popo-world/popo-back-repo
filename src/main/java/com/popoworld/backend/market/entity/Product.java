package com.popoworld.backend.market.entity;

import com.popoworld.backend.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue
    private UUID productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id")
    private User user; //null이면 먹이상점

    private String productName; //상품 이름
    private int productPrice; //상품 가격
    private int productStock; //재고
    private String productImage;

    @Enumerated(EnumType.STRING)
    private ProductStatus state; //REGISTERED, PURCHASE
    private int exp; //상품 경험치 (NPC 상품만 경험치 있음)

}
