package com.popoworld.backend.report.controller;

import com.popoworld.backend.report.dto.ReportResponseDTO;
import com.popoworld.backend.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "Report", description = "자녀 분석 리포트 API")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{childId}")
    @Operation(
            summary = "자녀 분석 리포트 조회",
            description = "부모가 자신의 자녀에 대한 분석 리포트를 조회합니다. " +
                    "분석 텍스트(투자/퀘스트/상점/전체 분석)와 그래프 데이터를 함께 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "리포트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                    {
                      "report": {
                        "id": "685d222878e57e8b64df4319",
                        "userId": "d4af0657-f9db-40ff-babd-68681db7ddeb",
                        "all": "전반적으로 아이는 투자와 상점 활동에서 활발한 참여를 보이며, 퀘스트에서도 높은 성공률을 기록하고 있습니다.",
                        "invest": "투자 영역에서 평균 체류 시간은 2.0으로, 전체 평균 1.6667에 비해 약 20% 높은 수치를 기록했습니다.",
                        "quest": "퀘스트 영역에서는 성공적으로 완료한 퀘스트 수가 10개로, 전체 평균 7개에 비해 약 42.9% 높은 수치를 기록했습니다.",
                        "shop": "상점 영역의 활동량은 평균 3.5로, 전체 평균 2.8에 비해 약 25% 높은 수치를 보였습니다."
                      },
                      "graph": {
                        "id": "685f734178e57e8b64e0f445",
                        "userId": "d4af0657-f9db-40ff-babd-68681db7ddeb",
                        "investGraph": [
                          {
                            "cluster_1": 30,
                            "cluster_2": 10,
                            "cluster_3": 13,
                            "cluster_4": 30,
                            "cluster_5": 26
                          }
                        ],
                        "questGraph": [
                          {
                            "daily_completion_rate_식탁 정리 도와주기": 0,
                            "daily_completion_rate_양치하기": 0,
                            "parent_completion_rate_STUDY": "NaN",
                            "parent_completion_rate_POPO": "NaN"
                          }
                        ],
                        "shopGraph": [
                          {
                            "day": "토",
                            "간식": 0,
                            "오락": 0,
                            "장난감": 0,
                            "교육 및 문구": 0,
                            "먹이": 0,
                            "기타": 0
                          },
                          {
                            "day": "일",
                            "간식": 0,
                            "오락": 0,
                            "장난감": 0,
                            "교육 및 문구": 0,
                            "먹이": 0,
                            "기타": 0
                          },
                          {
                            "day": "월",
                            "간식": 0,
                            "오락": 0,
                            "장난감": 0,
                            "교육 및 문구": 0,
                            "먹이": 400,
                            "기타": 0
                          }
                        ]
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 - 해당 자녀에 대한 접근 권한이 없습니다",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "권한 없음 응답",
                                    value = "{ \"error\": \"해당 자녀에 대한 접근 권한이 없습니다.\" }"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "데이터 없음 - 해당 자녀의 분석 리포트가 존재하지 않습니다",
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "데이터 없음 응답",
                                    value = "{ \"error\": \"분석 리포트를 찾을 수 없습니다.\" }"
                            )
                    )
            )
    })
    public ResponseEntity<ReportResponseDTO> getReport(
            @Parameter(
                    description = "자녀의 고유 ID (UUID 형식)",
                    required = true,
                    example = "d4af0657-f9db-40ff-babd-68681db7ddeb"
            )
            @PathVariable UUID childId
    ) {
        UUID parentId = getCurrentUserId();

        return reportService.getCombinedReport(parentId, childId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}