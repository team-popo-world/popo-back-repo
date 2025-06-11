package com.popoworld.backend.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WeekAttendanceResponse {
    private String dayOfWeek;
    private boolean isAttended;
}
