package com.popoworld.backend.attendance.controller;

import com.popoworld.backend.attendance.dto.AttendanceCheckResponse;
import com.popoworld.backend.attendance.dto.TodayAttendanceRequest;
import com.popoworld.backend.attendance.dto.WeekAttendanceResponse;
import com.popoworld.backend.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    //출석 현황 조회
    @GetMapping
    public ResponseEntity<List<WeekAttendanceResponse>> getAttendanceStatus() {
        UUID childId =getCurrentUserId();
        List<WeekAttendanceResponse> result = attendanceService.getAttendanceList(childId);
        return ResponseEntity.ok(result);
    }


    @PostMapping
//출석 체크
    public ResponseEntity<AttendanceCheckResponse> checkAttendance(@RequestBody TodayAttendanceRequest request){
        try{
            UUID childId = getCurrentUserId();
            AttendanceCheckResponse result = attendanceService.checkAttendance(childId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            // 에러 시에는 기존 방식대로 String 메시지만 반환하거나
            // 또는 빈 AttendanceCheckResponse 반환
            return ResponseEntity.badRequest().build(); // 또는 별도 에러 처리
        } catch (Exception e) {
            return ResponseEntity.badRequest().build(); // 또는 별도 에러 처리
        }
    }
}
