package com.popoworld.backend.invest.child.repository;

import com.popoworld.backend.invest.child.entity.InvestHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface InvestHistoryMongoRepository extends MongoRepository<InvestHistory, UUID> {
}