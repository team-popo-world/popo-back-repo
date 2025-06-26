package com.popoworld.backend.report.repository;

import com.popoworld.backend.report.Entity.ChildReportGraph;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ReportGraphRepository extends MongoRepository<ChildReportGraph, UUID> {
}
