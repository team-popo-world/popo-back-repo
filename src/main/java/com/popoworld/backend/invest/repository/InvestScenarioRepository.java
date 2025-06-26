package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestScenario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvestScenarioRepository extends JpaRepository<InvestScenario, UUID> {

    InvestScenario findByInvestChapter_ChapterId(String chapterId);

    InvestScenario findTopByUpdatedAtIsNullOrderByCreateAtAsc();

    //특정 챕터 중에서 가장 오래된 미업데이트 시나리오 찾기
    InvestScenario findTopByInvestChapter_ChapterIdAndUpdatedAtIsNullOrderByCreateAtAsc(String chapterId);

    //특정 챕터 + 커스텀 여부로 시나리오 리스트 조회
    List<InvestScenario> findByInvestChapter_ChapterIdAndIsCustom(String chapterId, Boolean isCustom);

    //특정 아이의 커스텀 시나리오 조회
    List<InvestScenario> findByChildIdAndInvestChapter_ChapterIdAndIsCustom(UUID childId, String chapterId, Boolean isCustom);

    //기본 시나리오 조회 (childId가 null인 경우)
    List<InvestScenario> findByChildIdIsNullAndInvestChapter_ChapterIdAndIsCustom(String chapterId, Boolean isCustom);

    //페이징 조회
    Page<InvestScenario> findByChildIdAndInvestChapter_ChapterId(UUID childId, String chapterId, Pageable pageable);

    @Query("SELECT s.scenarioName FROM InvestScenario s WHERE s.scenarioName LIKE '기본-%' ORDER BY s.scenarioName DESC LIMIT 1")
    Optional<String> findLastScenarioName();

}