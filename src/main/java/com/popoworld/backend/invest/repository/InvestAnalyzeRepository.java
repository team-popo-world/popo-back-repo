package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestAnalyze;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface InvestAnalyzeRepository extends MongoRepository<InvestAnalyze, UUID> {
}
