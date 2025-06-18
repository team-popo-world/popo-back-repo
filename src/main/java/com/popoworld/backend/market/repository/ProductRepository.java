package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    // 🔥 기존 메서드명 수정: user -> targetChild 의미 명확화
    @Query("SELECT p FROM Product p WHERE p.user.userId = :childId")
    List<Product> findByTargetChildId(@Param("childId") UUID childId);

    // 🔥 부모가 자녀들에게 등록한 상품들 조회
    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +  // p.user = 대상 자녀
            "WHERE u.parent.userId = :parentId")  // 자녀의 부모가 해당 부모
    List<Product> findByParentId(@Param("parentId") UUID parentId);

    // 🔥 새로 추가: 특정 부모의 특정 자녀용 상품만 조회
    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +
            "WHERE u.parent.userId = :parentId AND u.userId = :childId")
    List<Product> findByParentIdAndChildId(@Param("parentId") UUID parentId,
                                           @Param("childId") UUID childId);
    // NPC 상품 (기존 유지)
    List<Product> findByUserIsNull();
}
