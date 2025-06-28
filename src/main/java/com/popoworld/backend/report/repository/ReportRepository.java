package com.popoworld.backend.report.repository;

import com.popoworld.backend.report.entity.ChildReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends MongoRepository<ChildReport, String> {
    Optional<ChildReport> findByUserId(UUID userId);
}
