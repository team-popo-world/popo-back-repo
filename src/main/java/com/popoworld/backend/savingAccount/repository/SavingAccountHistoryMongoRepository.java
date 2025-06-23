package com.popoworld.backend.savingAccount.repository;

import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SavingAccountHistoryMongoRepository extends MongoRepository<SavingAccountHistory, UUID> {
    // 자녀의 모든 입금 내역 조회 (최신순)
    @Query("{'childId': ?0, 'eventType': 'DEPOSIT'}")
    List<SavingAccountHistory> findAllDepositsByChildIdOrderByTimestampDesc(UUID childId);
}
