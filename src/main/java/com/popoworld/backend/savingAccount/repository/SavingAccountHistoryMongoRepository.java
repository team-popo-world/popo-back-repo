package com.popoworld.backend.savingAccount.repository;

import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SavingAccountHistoryMongoRepository extends MongoRepository<SavingAccountHistory, UUID> {
    // 자녀의 모든 입금 내역 조회 (최신순)
    @Query(value = "{'childId': ?0, 'eventType': 'DEPOSIT'}", sort = "{'timestamp': -1}")
    List<SavingAccountHistory> findAllDepositsByChildIdOrderByTimestampDesc(UUID childId);

    // 🆕 특정 저축통장의 입금내역만 조회 (savingAccountId로!)
    @Query(value = "{'savingAccountId': ?0, 'eventType': 'DEPOSIT'}", sort = "{'timestamp': -1}")
    List<SavingAccountHistory> findDepositsBySavingAccountId(UUID savingAccountId);
}
