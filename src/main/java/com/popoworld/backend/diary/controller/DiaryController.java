package com.popoworld.backend.diary.controller;

import com.popoworld.backend.diary.dto.DiaryListResponse;
import com.popoworld.backend.diary.dto.DiaryTodayRequest;
import com.popoworld.backend.diary.service.EmotionDiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diary")
@Tag(name = "EmotionDiary", description = "감정 일기 관리 API")
public class DiaryController {
    private final EmotionDiaryService emotionDiaryService;

    @PostMapping
    @Operation(
            summary = "감정 일기 등록",
            description = "아이의 오늘 감정과 한줄 일기를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "감정 일기 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 오늘 이미 일기 작성됨"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<String> createEmotionDiary(@RequestBody DiaryTodayRequest request){

        try {
            // 임시로 고정된 childId 사용 (나중에 JWT에서 추출 예정)
            UUID childId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

            emotionDiaryService.createEmotionDiary(childId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body("감정 일기가 성공적으로 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 오류가 발생했습니다.");
        }
    }

    @GetMapping
    @Operation(
            summary = "감정 일기 목록 조회",
            description = "아이의 모든 감정 일기 목록을 최신순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "감정 일기 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<List<DiaryListResponse>>getEmotionDiaries(){
        // 임시로 고정된 childId 사용 (나중에 JWT에서 추출 예정)
        UUID childId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        List<DiaryListResponse> diaries = emotionDiaryService.getEmotionDiariesByChildId(childId);
        return ResponseEntity.ok(diaries);
    }
}
