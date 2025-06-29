package com.popoworld.backend.report.repository;

import com.popoworld.backend.report.entity.ChildReportGraph;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportGraphRepository extends MongoRepository<ChildReportGraph, String> {
    Optional<ChildReportGraph> findByUserId(UUID userId);

}
