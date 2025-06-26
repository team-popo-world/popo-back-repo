package com.popoworld.backend.report.service;

import com.popoworld.backend.report.entity.ChildReport;
import com.popoworld.backend.report.entity.ChildReportGraph;
import com.popoworld.backend.report.repository.ReportGraphRepository;
import com.popoworld.backend.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportGraphRepository graphRepository;

    public Optional<ChildReport> getReport(UUID childId) {
        return reportRepository.findById(childId);
    }

    public Optional<ChildReportGraph> getReportGraph(UUID childId) {
        return graphRepository.findById(childId);
    }
}
