package com.popoworld.backend.savingAccount.scheduler;

import com.popoworld.backend.savingAccount.service.SavingAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SavingAccountScheduler {
    private final SavingAccountService savingAccountService;

    // 매일 자정에 만료된 저축통장 비활성화
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deactivateExpiredAccounts() {
        log.info("🏦 만료된 저축통장 비활성화 작업 시작");

        try {
            savingAccountService.deactivateExpiredAccounts();
            log.info("✅ 만료된 저축통장 비활성화 작업 완료");
        } catch (Exception e) {
            log.error("❌ 만료된 저축통장 비활성화 작업 실패", e);
        }
    }
}
