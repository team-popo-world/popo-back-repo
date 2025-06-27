package com.popoworld.backend.report.controller;

import com.popoworld.backend.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name="Report", description = "자녀 레포트")
public class ReportController {

    private final ReportService reportService;

    @PostMapping()
    public ResponseEntity<?> getReport(@RequestBody UUID childId) {
        return reportService.getCombinedReport(childId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
