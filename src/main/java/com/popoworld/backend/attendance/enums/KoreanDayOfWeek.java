package com.popoworld.backend.attendance.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;

@Getter
@RequiredArgsConstructor
public enum KoreanDayOfWeek {
    MONDAY("월", DayOfWeek.MONDAY),
    TUESDAY("화", DayOfWeek.TUESDAY),
    WEDNESDAY("수", DayOfWeek.WEDNESDAY),
    THURSDAY("목", DayOfWeek.THURSDAY),
    FRIDAY("금", DayOfWeek.FRIDAY),
    SATURDAY("토", DayOfWeek.SATURDAY),
    SUNDAY("일", DayOfWeek.SUNDAY);

    private final String korean;
    private final DayOfWeek javaDay;

    //한글로 찾기
    public static KoreanDayOfWeek fromKorean(String korean){
        for(KoreanDayOfWeek day: values()){
            if(day.korean.equals(korean)){
                return day;
            }
        }
        throw new IllegalArgumentException("잘못된 요일"+korean);
    }
}
