package com.popoworld.backend.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AttendanceCheckResponse {
    private List<WeekAttendanceResponse> weekAttendance; //업데이트된 주간 출석 현황
    private boolean weekCompleted; //일주일 완주 여부
    private int rewardPoints; //받은 보상 포인트
}
