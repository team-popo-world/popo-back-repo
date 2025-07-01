package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvestSessionRepository extends JpaRepository<InvestSession, UUID> {
    @Query(value = "SELECT DISTINCT scenario_id FROM invest_session " +
            "WHERE child_id = :childId AND scenario_id IN :scenarioIds", nativeQuery = true)
    List<UUID> findExecutedScenarioIdsByChildIdAndScenarioIds(
            @Param("childId") UUID childId,
            @Param("scenarioIds") List<UUID> scenarioIds);

}