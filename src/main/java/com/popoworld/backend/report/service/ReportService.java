package com.popoworld.backend.report.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
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
    private final UserRepository userRepository;

    public Optional<ReportResponseDTO> getCombinedReport(UUID parentId, UUID childId) {
        // 부모-자녀 관계 확인
        if (!isValidParentChild(parentId, childId)) {
            return Optional.empty();
        }

        // 기존 로직 그대로
        Optional<ChildReport> reportOpt = reportRepository.findByUserId(childId);
        Optional<ChildReportGraph> graphOpt = graphRepository.findByUserId(childId);

        if (reportOpt.isPresent() && graphOpt.isPresent()) {
            return Optional.of(new ReportResponseDTO(reportOpt.get(), graphOpt.get()));
        } else {
            return Optional.empty();
        }
    }
    // 권한 검증 메서드
    private boolean isValidParentChild(UUID parentId, UUID childId) {
        Optional<User> childOpt = userRepository.findById(childId);
        if (childOpt.isEmpty()) {
            return false;
        }

        User child = childOpt.get();
        return child.getParent() != null &&
                child.getParent().getUserId().equals(parentId);
    }

}
