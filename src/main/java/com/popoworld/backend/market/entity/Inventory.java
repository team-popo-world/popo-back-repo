package com.popoworld.backend.market.entity;

import com.popoworld.backend.User.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    private int stock;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Inventory(UUID inventoryId, User user, Product product, Integer stock) {
        this.inventoryId = inventoryId;
        this.user = user;
        this.product = product;
        this.stock = stock;
    }
}
