package com.popoworld.backend.invest.repository;

import com.popoworld.backend.invest.entity.InvestHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InvestHistoryMongoRepository extends MongoRepository<InvestHistory, UUID> {
}