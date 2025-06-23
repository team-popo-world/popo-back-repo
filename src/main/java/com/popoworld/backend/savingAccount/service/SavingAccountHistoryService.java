package com.popoworld.backend.savingAccount.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.savingAccount.entity.SavingAccount;
import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import com.popoworld.backend.savingAccount.savingAccountHistoryKafka.SavingAccountHistoryKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingAccountHistoryService {
    private final SavingAccountHistoryKafkaProducer savingAccountHistoryKafkaProducer;

    /**
     * 저축통장 생성 로그
     */
    public void logAccountCreation(SavingAccount savingAccount) {
        try {
            SavingAccountHistory history = new SavingAccountHistory(
                    UUID.randomUUID(),                          // 1. id
                    savingAccount.getChild().getUserId(),       // 2. childId
                    savingAccount.getSavingAccountId(),
                    savingAccount.getAccountPoint(),            // 3. accountPoint
                    savingAccount.getGoalAmount(),              // 4. goalAmount
                    savingAccount.getRewardPoint(),             // 5. rewardPoint
                    null,                                       // 6. dailyDepositAmount (생성 시 null)
                    savingAccount.getCreatedDate(),             // 7. createdDate
                    savingAccount.getEndDate(),                 // 8. endDate
                    savingAccount.getSuccess(),                 // 9. success
                    "ACCOUNT_CREATED",                          // 10. eventType ✅
                    LocalDateTime.now(ZoneId.of("Asia/Seoul")), // 11. timestamp
                    0,                                          // 12. percent
                    savingAccount.getActive()                   // 13. active
            );

            sendToKafka(history);
            log.info("저축통장 생성 로그 전송 완료");
        } catch (Exception e) {
            log.error("저축통장 생성 로그 전송 실패", e);
        }
    }

    /**
     * 입금 로그
     */
    public void logDeposit(SavingAccount savingAccount, Integer depositAmount, Integer currentAccountPoint) {
        try {
            Integer percent = calculatePercent(currentAccountPoint, savingAccount.getGoalAmount());

            SavingAccountHistory history = new SavingAccountHistory(
                    UUID.randomUUID(),                          // 1. id
                    savingAccount.getChild().getUserId(),       // 2. childId
                    savingAccount.getSavingAccountId(),
                    currentAccountPoint,                        // 3. accountPoint
                    savingAccount.getGoalAmount(),              // 4. goalAmount
                    savingAccount.getRewardPoint(),             // 5. rewardPoint
                    depositAmount,                              // 6. dailyDepositAmount
                    savingAccount.getCreatedDate(),             // 7. createdDate
                    savingAccount.getEndDate(),                 // 8. endDate
                    savingAccount.getSuccess(),                 // 9. success
                    "DEPOSIT",                                  // 10. eventType ✅
                    LocalDateTime.now(ZoneId.of("Asia/Seoul")), // 11. timestamp ✅
                    percent,                                    // 12. percent ✅
                    savingAccount.getActive()                   // 13. active ✅
            );

            sendToKafka(history);
            log.info("💰 입금 로그 전송 완료 - 입금액: {}, 달성률: {}%", depositAmount, percent);
        } catch (Exception e) {
            log.error("❌ 입금 로그 전송 실패", e);
        }
    }

    /**
     * 목표 달성 로그
     */
    public void logGoalAchieved(SavingAccount savingAccount) {
        try {
            SavingAccountHistory history = new SavingAccountHistory(
                    UUID.randomUUID(),                          // 1. id
                    savingAccount.getChild().getUserId(),       // 2. childId
                    savingAccount.getSavingAccountId(),
                    savingAccount.getAccountPoint(),            // 3. accountPoint
                    savingAccount.getGoalAmount(),              // 4. goalAmount
                    savingAccount.getRewardPoint(),             // 5. rewardPoint
                    null,                                       // 6. dailyDepositAmount
                    savingAccount.getCreatedDate(),             // 7. createdDate
                    savingAccount.getEndDate(),                 // 8. endDate
                    true,                                       // 9. success (목표 달성)
                    "GOAL_ACHIEVED",                            // 10. eventType ✅
                    LocalDateTime.now(ZoneId.of("Asia/Seoul")), // 11. timestamp ✅
                    100,                                        // 12. percent ✅ (100% 달성)
                    false                                       // 13. active ✅ (달성 시 비활성화)
            );

            sendToKafka(history);
            log.info("🎉 목표 달성 로그 전송 완료 - 보상: {}", savingAccount.getRewardPoint());
        } catch (Exception e) {
            log.error("❌ 목표 달성 로그 전송 실패", e);
        }
    }

    /**
     * 만료 로그
     */
    public void logAccountExpired(SavingAccount savingAccount) {
        try {
            Integer percent = calculatePercent(savingAccount.getAccountPoint(), savingAccount.getGoalAmount());

            SavingAccountHistory history = new SavingAccountHistory(
                    UUID.randomUUID(),                          // 1. id
                    savingAccount.getChild().getUserId(),       // 2. childId
                    savingAccount.getSavingAccountId(),
                    savingAccount.getAccountPoint(),            // 3. accountPoint
                    savingAccount.getGoalAmount(),              // 4. goalAmount
                    savingAccount.getRewardPoint(),             // 5. rewardPoint
                    null,                                       // 6. dailyDepositAmount
                    savingAccount.getCreatedDate(),             // 7. createdDate
                    savingAccount.getEndDate(),                 // 8. endDate
                    false,                                      // 9. success (만료로 실패)
                    "ACCOUNT_EXPIRED",                          // 10. eventType ✅
                    LocalDateTime.now(ZoneId.of("Asia/Seoul")), // 11. timestamp ✅
                    percent,                                    // 12. percent ✅
                    false                                       // 13. active ✅ (만료 시 비활성화)
            );

            sendToKafka(history);
            log.info("저축통장 만료 로그 전송 완료 - 최종 달성률: {}%", percent);
        } catch (Exception e) {
            log.error("저축통장 만료 로그 전송 실패", e);
        }
    }

    /**
     * 달성률 계산
     */
    private Integer calculatePercent(Integer currentAmount, Integer goalAmount) {
        if (goalAmount == null || goalAmount == 0) {
            return 0;
        }
        return Math.min(100, (currentAmount * 100) / goalAmount);
    }

    /**
     * Kafka로 메시지 전송
     */
    // SavingAccountHistoryService.java의 sendToKafka 메서드에서
    private void sendToKafka(SavingAccountHistory history) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS); // ✅ null 값도 포함
        String json = mapper.writeValueAsString(history);

        savingAccountHistoryKafkaProducer.sendSavingAccountHistory("saving-account-history", json);
    }
}