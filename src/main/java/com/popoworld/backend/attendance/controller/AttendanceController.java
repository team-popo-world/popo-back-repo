package com.popoworld.backend.attendance.controller;

import com.popoworld.backend.attendance.dto.TodayAttendanceRequest;
import com.popoworld.backend.attendance.dto.WeekAttendanceResponse;
import com.popoworld.backend.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    //출석 현황 조회
    @GetMapping
    public ResponseEntity<List<WeekAttendanceResponse>> getAttendanceStatus() {
        UUID childId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        List<WeekAttendanceResponse> result = attendanceService.getAttendanceList(childId);
        return ResponseEntity.ok(result);
    }


    @PostMapping
    //출석 체크
    public ResponseEntity<String>checkAttendance(@RequestBody TodayAttendanceRequest request){
        try{
            UUID childId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String result = attendanceService.checkAttendance(childId,request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage()); // 이미 출석한 경우
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("잘못된 요청");
        }
    }
}
