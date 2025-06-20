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
    @Query("SELECT p FROM Product p WHERE p.user.userId = :childId")
    List<Product> findByTargetChildId(@Param("childId") UUID childId);

    // 부모가 자녀들에게 등록한 상품들 조회
    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +  // p.user = 대상 자녀
            "WHERE u.parent.userId = :parentId")  // 자녀의 부모가 해당 부모
    List<Product> findByParentId(@Param("parentId") UUID parentId);

    // 특정 부모의 특정 자녀용 상품만 조회
    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +
            "WHERE u.parent.userId = :parentId AND u.userId = :childId")
    List<Product> findByParentIdAndChildId(@Param("parentId") UUID parentId,
                                           @Param("childId") UUID childId);
    // NPC 상품 조회
    List<Product> findByUserIsNull();

    // 부모 상품 승인 관련 쿼리들
    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +
            "WHERE u.parent.userId = :parentId AND p.state = 'USED' " +
            "ORDER BY p.updatedAt DESC")
    List<Product> findPendingApprovalsByParentId(@Param("parentId") UUID parentId);

    @Query("SELECT p FROM Product p " +
            "WHERE p.user.userId = :childId AND p.state = 'USED' " +
            "ORDER BY p.updatedAt DESC")
    List<Product> findPendingApprovalsByChildId(@Param("childId") UUID childId);

    @Query("SELECT p FROM Product p " +
            "JOIN p.user u " +
            "WHERE u.parent.userId = :parentId AND p.state = 'APPROVED' " +
            "AND (:childId IS NULL OR u.userId = :childId) " +
            "ORDER BY p.updatedAt DESC")
    List<Product> findApprovedUsageByParentId(@Param("parentId") UUID parentId,
                                              @Param("childId") UUID childId);
}
