package com.popoworld.backend.attendance.controller;

import com.popoworld.backend.attendance.dto.AttendanceCheckResponse;
import com.popoworld.backend.attendance.dto.TodayAttendanceRequest;
import com.popoworld.backend.attendance.dto.WeekAttendanceResponse;
import com.popoworld.backend.attendance.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Attendance", description = "출석 체크 및 현황 조회 API")
public class AttendanceController {
    private final AttendanceService attendanceService;

    //출석 현황 조회
    @GetMapping
    @Operation(summary = "출석 현황 조회", description = "이번 주 출석 현황을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public ResponseEntity<List<WeekAttendanceResponse>> getAttendanceStatus() {
        UUID childId =getCurrentUserId();
        List<WeekAttendanceResponse> result = attendanceService.getAttendanceList(childId);
        return ResponseEntity.ok(result);
    }


    @PostMapping
    @Operation(summary = "출석 체크", description = "출석을 체크합니다. 일주일 완주 시 보상 포인트를 지급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "출석 체크 성공",
                    content = @Content(schema = @Schema(implementation = AttendanceCheckResponse.class))),
            @ApiResponse(responseCode = "400", description = "이미 출석했거나 잘못된 요청")
    })
    public ResponseEntity<AttendanceCheckResponse> checkAttendance(
            @RequestBody @Valid TodayAttendanceRequest request) {
        try {
            UUID childId = getCurrentUserId();
            AttendanceCheckResponse result = attendanceService.checkAttendance(childId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
