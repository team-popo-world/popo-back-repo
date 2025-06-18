package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    //Optional<Inventory>findByUserAndProduct(User user, Product product);

    // 올바른 방식 (필드명에 맞춤)
    Optional<Inventory> findByUser_UserIdAndProduct_ProductId(UUID userId, UUID productId);

    // 사용자의 모든 인벤토리 아이템 조회
    List<Inventory> findByUser_UserId(UUID userId);
}
