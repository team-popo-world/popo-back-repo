package com.popoworld.backend.report.repository;

import com.popoworld.backend.report.Entity.ChildReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ReportRepository extends MongoRepository<ChildReport, UUID> {
}
