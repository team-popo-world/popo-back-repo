package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByUser(UUID childId); //부모가 등록한 상품 찾기

    List<Product> findByUserIsNull(); //먹이 상품 리스트
}
