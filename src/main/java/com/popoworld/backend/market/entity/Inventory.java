package com.popoworld.backend.market.entity;

import com.popoworld.backend.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inventory_user_product",
                        columnNames = {"user_id", "product_id"}
                )
        }
)
public class Inventory {
    @Id
    @GeneratedValue
    @Column(name="inventory_id")
    private UUID inventoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //한 사용자당 하나의 인벤토리

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int stock; //NPC 상품은 실제 수량, 부모 상품은 항상 1

    @CreationTimestamp
    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;
}
