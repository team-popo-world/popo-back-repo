package com.popoworld.backend.report.controller;

import com.popoworld.backend.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name="Report", description = "자녀 레포트")
public class ReportController {

    private final ReportService reportService;

//    @Operation(summary = "레포트 요청", description = "레포트 요청 api")
//    @GetMapping
//    public
}
