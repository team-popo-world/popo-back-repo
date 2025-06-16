package com.popoworld.backend.quest.repository;

import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {
    //기본 조회
    //아이별 퀘스트 조회
    List<Quest> findByChildId(UUID childId);
    //아이별+타입별 퀘스트 조회
    List<Quest> findByChildIdAndType(UUID childId, Quest.QuestType type);

    //스케줄러용 - 일일퀘스트 삭제
    @Modifying //select가 아니라 db에 변경을 가하는 쿼리임을 나타냄
    @Query("DELETE FROM Quest q WHERE q.type = :type")
    void deleteByType(@Param("type")Quest.QuestType type);

    /**
     * 특정 아이의 만료 가능한 부모퀘스트 조회
     * (만료시간이 지났지만 아직 EXPIRED 상태가 아닌 퀘스트들)
     */
    @Query("SELECT q FROM Quest q WHERE q.childId = :childId " +
            "AND q.type = :questType " +
            "AND q.endDate < :now " +
            "AND q.state NOT IN ('COMPLETED', 'EXPIRED')")
    List<Quest> findExpirableParentQuests(
            @Param("childId") UUID childId,
            @Param("questType") Quest.QuestType questType,
            @Param("now") LocalDateTime now
    );

    /**
     * 전체 만료 가능한 부모퀘스트 조회 (스케줄러 백업용)
     */
    @Query("SELECT q FROM Quest q WHERE q.type = :questType " +
            "AND q.endDate < :now " +
            "AND q.state NOT IN ('COMPLETED', 'EXPIRED')")
    List<Quest> findAllExpirableParentQuests(
            @Param("questType") Quest.QuestType questType,
            @Param("now") LocalDateTime now
    );

    // 기존 스케줄러용 메서드 (사용 안함으로 변경 예정)
    @Modifying
    @Query("UPDATE Quest q SET q.state = :expiredState WHERE q.type = :questType AND q.endDate < :now AND q.state = :activeState")
    int updateExpiredParentQuests(
            @Param("now") LocalDateTime now,
            @Param("questType") Quest.QuestType questType,
            @Param("expiredState") QuestState expiredState,
            @Param("activeState") QuestState activeState  // ACTIVE 상태만 만료 처리
    );
}