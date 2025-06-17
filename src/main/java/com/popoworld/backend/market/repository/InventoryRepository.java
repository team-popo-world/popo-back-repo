package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Inventory;
import com.popoworld.backend.market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    List<Inventory> findAll(); // 전체 inventory

    Optional<Inventory> findByProduct(Product product);

    void deleteByProduct(Product product);
}
