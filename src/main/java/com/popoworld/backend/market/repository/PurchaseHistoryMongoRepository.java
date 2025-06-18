package com.popoworld.backend.market.repository;

import com.popoworld.backend.market.entity.PurchaseHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PurchaseHistoryMongoRepository extends MongoRepository<PurchaseHistory, String> {
}
