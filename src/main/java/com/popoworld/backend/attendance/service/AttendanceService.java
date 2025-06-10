package com.popoworld.backend.attendance.service;

import com.popoworld.backend.attendance.dto.TodayAttendanceRequest;
import com.popoworld.backend.attendance.dto.WeekAttendanceResponse;
import com.popoworld.backend.attendance.entity.DailyCheck;
import com.popoworld.backend.attendance.enums.KoreanDayOfWeek;
import com.popoworld.backend.attendance.repository.DailyCheckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    //출석 현황 조회
    public List<WeekAttendanceResponse>getAttendanceList(UUID childId){
        LocalDate today = LocalDate.now();
        //이번주의 월요일부터 일요일까지 날짜 범위 구하는 코드
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        //오늘이 월요일이면 오늘을 반환, 오늘이 화~일요일이면 그 주의 월요일을 찾아 반환
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        //-> 이번 주 출석 기록을 조회하는데 쓰인다

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

    // POST - 출석 체크
    public String checkAttendance(UUID childId, TodayAttendanceRequest request) {
        System.out.println("=== 디버그 정보 ===");
        System.out.println("dayOfWeek: " + request.getDayOfWeek());
        System.out.println("isAttended: " + request.isAttended());
        System.out.println("==================");

        // 요일 검증
        KoreanDayOfWeek dayEnum = KoreanDayOfWeek.fromKorean(request.getDayOfWeek());

        // 해당 요일 날짜 계산
        LocalDate targetDate = getThisWeekDate(dayEnum);
        System.out.println("targetDate: " + targetDate);

        // 이미 출석했는지 확인
        boolean exists = dailyCheckRepository.existsByChildIdAndAttendanceDate(childId, targetDate);
        System.out.println("already exists: " + exists);

        if (exists) {
            throw new IllegalArgumentException("이미 " + request.getDayOfWeek() + "요일에 출석했습니다.");
        }

        // 출석 기록 저장
        System.out.println("isAttended check: " + request.isAttended());
        if (request.isAttended()) {
            DailyCheck check = new DailyCheck(null, childId, targetDate);
            dailyCheckRepository.save(check);
            System.out.println("출석 기록 저장 완료!");

            // 일주일 완주 확인
            if (isWeekCompleted(childId)) {
                return "🎉 일주일 완주! 보상 100점!";
            } //이때 지금은 그냥 이렇게 하지만 포인트 생기면 거기 추가해야됨
            return "출석 완료!";
        }

        System.out.println("isAttended가 false여서 출석하지 않음 반환");
        return "출석하지 않음";
    }
    private LocalDate getThisWeekDate(KoreanDayOfWeek dayEnum) {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
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
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        List<DailyCheck> checks = dailyCheckRepository.findByChildIdAndAttendanceDateBetween(childId, monday, sunday);
        return checks.size() == 7;
    }

}
