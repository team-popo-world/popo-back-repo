package com.popoworld.backend.attendance.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class TodayAttendanceRequest {
    private String dayOfWeek;
    @JsonProperty("isAttended")  // ← Jackson에게 명시적으로 알려주기
    private boolean isAttended;
}
