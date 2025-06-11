package com.popoworld.backend.attendance.repository;

import com.popoworld.backend.attendance.entity.DailyCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DailyCheckRepository extends JpaRepository<DailyCheck, UUID> {
    boolean existsByChildIdAndAttendanceDate(UUID childId, LocalDate date);

    // 이번 주 출석 기록 가져오기
    List<DailyCheck> findByChildIdAndAttendanceDateBetween(
            UUID childId, LocalDate startDate, LocalDate endDate);
}
