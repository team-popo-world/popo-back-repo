package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByUser_UserIdAndProduct_ProductId(UUID userId, UUID productId);
    List<Inventory> findByUser_UserId(UUID userId);
}

