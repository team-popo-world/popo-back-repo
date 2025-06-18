package com.popoworld.backend.market.entity;

import com.popoworld.backend.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="product_usage")
public class ProductUsage { //사용내역을 저장할 테이블

    @Id
    @GeneratedValue
    @Column(name="usage_id")
    private UUID usageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private User child; //사용한 자녀

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; //사용한 상품

    private int usedAmount; //사용한 수량

    @CreationTimestamp
    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt; // 사용 시간
}
