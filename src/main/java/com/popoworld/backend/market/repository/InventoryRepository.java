package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    //특정 사용자의 특정 상품 인벤토리 조회(NPC상품 구매 시 기존 것 찾기용)
    Optional<Inventory> findByUser_UserIdAndProduct_ProductId(UUID userId, UUID productId);

   //사용자의 모든 인벤토리 조회
    List<Inventory> findByUser_UserId(UUID userId);
}

