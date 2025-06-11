package com.popoworld.backend.diary.child.repository;

import com.popoworld.backend.diary.child.entity.EmotionDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, UUID> {
    //특정 아이의 오늘 일기가 있는지 확인
    boolean existsByChildIdAndCreatedAt(UUID childId, LocalDate createdAt);

    //특정 아이의 모든 감정일기 조회
    List<EmotionDiary> findByChildIdOrderByCreatedAtDesc(UUID childId);
}
