package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.ProductUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductUsageRepository extends JpaRepository<ProductUsage, UUID> {
    // 🔥 핵심: User 테이블의 parent 관계 활용!
    @Query("SELECT pu FROM ProductUsage pu " +
            "JOIN pu.child c " +  // 사용한 자녀
            "WHERE c.parent.userId = :parentId " +  // 자녀의 부모가 해당 부모인지 확인
            "AND (:childId IS NULL OR c.userId = :childId) " +
            "ORDER BY pu.usedAt DESC")
    List<ProductUsage> findByParentIdAndChildId(@Param("parentId") UUID parentId, @Param("childId") UUID childId);
}
