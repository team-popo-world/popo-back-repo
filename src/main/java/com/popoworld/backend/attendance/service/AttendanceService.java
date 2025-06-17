package com.popoworld.backend.attendance.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.attendance.dto.AttendanceCheckResponse;
import com.popoworld.backend.attendance.dto.TodayAttendanceRequest;
import com.popoworld.backend.attendance.dto.WeekAttendanceResponse;
import com.popoworld.backend.attendance.entity.DailyCheck;
import com.popoworld.backend.attendance.enums.KoreanDayOfWeek;
import com.popoworld.backend.attendance.repository.DailyCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final DailyCheckRepository dailyCheckRepository;
    private final UserRepository userRepository; // Users 테이블 접근용

    // 한국 시간대 설정
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    // 매일 출석 보상 포인트
    private static final int DAILY_ATTENDANCE_REWARD = 100;

    // 일주일 완주 추가 보상 포인트
    private static final int WEEK_COMPLETION_REWARD = 500;

    // 한국 시간 기준 오늘 날짜 가져오기
    private LocalDate getKoreaToday() {
        return LocalDate.now(KOREA_ZONE);
    }

    //출석 현황 조회
    public List<WeekAttendanceResponse> getAttendanceList(UUID childId) {
        LocalDate today = getKoreaToday(); // 한국 시간 적용

        //이번주의 월요일부터 일요일까지 날짜 범위 구하는 코드
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        //이번 주 출석 기록 가져오기
        List<DailyCheck> checks = dailyCheckRepository.findByChildIdAndAttendanceDateBetween(childId, monday, sunday);
        Set<LocalDate> attendedDates = checks.stream()
                .map(DailyCheck::getAttendanceDate)
                .collect(Collectors.toSet());

        List<WeekAttendanceResponse> result = new ArrayList<>();
        LocalDate date = monday;

        for (KoreanDayOfWeek dayEnum : KoreanDayOfWeek.values()) {
            boolean isAttended = attendedDates.contains(date);
            result.add(new WeekAttendanceResponse(dayEnum.getKorean(), isAttended));
            date = date.plusDays(1);
        }

        return result;
    }

    @Transactional
    public AttendanceCheckResponse checkAttendance(UUID childId, TodayAttendanceRequest request) {
        System.out.println("=== 디버그 정보 ===");
        System.out.println("dayOfWeek: " + request.getDayOfWeek());
        System.out.println("isAttended: " + request.isAttended());
        System.out.println("Korea time: " + getKoreaToday());
        System.out.println("==================");

        // 요일 검증
        KoreanDayOfWeek dayEnum = KoreanDayOfWeek.fromKorean(request.getDayOfWeek());

        // 해당 요일 날짜 계산 (한국 시간 기준)
        LocalDate targetDate = getThisWeekDate(dayEnum);
        System.out.println("targetDate: " + targetDate);

        // 이미 출석했는지 확인
        boolean exists = dailyCheckRepository.existsByChildIdAndAttendanceDate(childId, targetDate);
        System.out.println("already exists: " + exists);

        if (exists) {
            throw new IllegalArgumentException("이미 " + request.getDayOfWeek() + "요일에 출석했습니다.");
        }

        boolean weekCompleted = false;
        int rewardPoints = 0;

        // 출석 기록 저장
        System.out.println("isAttended check: " + request.isAttended());
        if (request.isAttended()) {
            DailyCheck check = new DailyCheck(null, childId, targetDate);
            dailyCheckRepository.save(check);
            System.out.println("출석 기록 저장 완료!");

            // 매일 출석 보상 지급 (100원)
            rewardPoints = DAILY_ATTENDANCE_REWARD;
            addPointsToUser(childId, DAILY_ATTENDANCE_REWARD);
            System.out.println("매일 출석 보상 " + DAILY_ATTENDANCE_REWARD + "원 지급!");

            // 일주일 완주 확인 및 추가 보상 지급
            if (isWeekCompleted(childId)) {
                weekCompleted = true;
                rewardPoints += WEEK_COMPLETION_REWARD; // 추가 보상

                addPointsToUser(childId, WEEK_COMPLETION_REWARD);
                System.out.println("🎉 일주일 완주! 추가 보상 " + WEEK_COMPLETION_REWARD + "원!");
                System.out.println("총 받은 포인트: " + rewardPoints + "원");
            }
        } else {
            System.out.println("isAttended가 false여서 출석하지 않음");
        }

        // 업데이트된 주간 출석 현황 조회
        List<WeekAttendanceResponse> weekAttendance = getAttendanceList(childId);

        return new AttendanceCheckResponse(weekAttendance, weekCompleted, rewardPoints);
    }
    private LocalDate getThisWeekDate(KoreanDayOfWeek dayEnum) {
        LocalDate monday = getKoreaToday().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return switch (dayEnum) {
            case MONDAY -> monday;
            case TUESDAY -> monday.plusDays(1);
            case WEDNESDAY -> monday.plusDays(2);
            case THURSDAY -> monday.plusDays(3);
            case FRIDAY -> monday.plusDays(4);
            case SATURDAY -> monday.plusDays(5);
            case SUNDAY -> monday.plusDays(6);
        };
    }

    // 일주일 완주 확인
    private boolean isWeekCompleted(UUID childId) {
        LocalDate monday = getKoreaToday().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        List<DailyCheck> checks = dailyCheckRepository.findByChildIdAndAttendanceDateBetween(childId, monday, sunday);
        return checks.size() == 7;
    }

    // 사용자에게 포인트 추가
    private void addPointsToUser(UUID childId, int points) {
        try {
            User user = userRepository.findById(childId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            user.setPoint(user.getPoint() + points);
            userRepository.save(user);

            System.out.println("포인트 지급 완료: " + points + "원");
        } catch (Exception e) {
            System.err.println("포인트 지급 실패: " + e.getMessage());
            // 포인트 지급 실패해도 출석은 성공으로 처리
        }
    }
}