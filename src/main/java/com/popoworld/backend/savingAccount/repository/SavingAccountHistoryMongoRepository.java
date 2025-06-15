package com.popoworld.backend.savingAccount.repository;

import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SavingAccountHistoryMongoRepository extends MongoRepository<SavingAccountHistory, UUID> {
}
