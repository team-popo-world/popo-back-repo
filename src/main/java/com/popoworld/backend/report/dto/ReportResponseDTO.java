package com.popoworld.backend.report.dto;

import com.popoworld.backend.report.entity.ChildReport;
import com.popoworld.backend.report.entity.ChildReportGraph;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportResponseDTO {
    private ChildReport report;
    private ChildReportGraph graph;
}
