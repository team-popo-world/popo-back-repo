package com.popoworld.backend.report.service;

import com.popoworld.backend.report.dto.ReportResponseDTO;
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

    public Optional<ReportResponseDTO> getCombinedReport(UUID childId) {
        Optional<ChildReport> reportOpt = reportRepository.findById(childId);
        Optional<ChildReportGraph> graphOpt = graphRepository.findById(childId);

        if (reportOpt.isPresent() && graphOpt.isPresent()) {
            return Optional.of(new ReportResponseDTO(reportOpt.get(), graphOpt.get()));
        } else {
            return Optional.empty();
        }
    }

}
