package com.popoworld.backend.savingAccount.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.savingAccount.dto.AccountCreateRequest;
import com.popoworld.backend.savingAccount.dto.AccountUserResponse;
import com.popoworld.backend.savingAccount.dto.DailyDepositRequest;
import com.popoworld.backend.savingAccount.dto.DailyDepositResponse;
import com.popoworld.backend.savingAccount.entity.SavingAccount;
import com.popoworld.backend.savingAccount.repository.SavingAccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingAccountService {
    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;
    private final SavingAccountHistoryService savingAccountHistoryService;

    public String createSavingAccount(UUID childId, AccountCreateRequest request) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(savingAccountRepository.existsActiveSavingAccount(child)) {
            throw new IllegalArgumentException("이미 활성화된 저축 통장이 있습니다.");
        }

        SavingAccount savingAccount = buildSaveAccountEntity(request, child);
        SavingAccount savedAccount = savingAccountRepository.save(savingAccount);

        savingAccountHistoryService.logAccountCreation(savedAccount);

        return "저축 통장이 성공적으로 생성되었습니다.";
    }

    private SavingAccount buildSaveAccountEntity(AccountCreateRequest request, User child) {
        return new SavingAccount(
                null,                           // savingAccountId
                child,                          // child
                0,                              // accountPoint
                request.getGoalAmount(),        // goalAmount
                request.getRewardPoint(),       // rewardPoint
                request.getCreatedAt(),         // createdDate
                request.getEndDate(),           // endDate
                false,                          // success (초기 성공 여부는 false)
                true,                           // active (초기 활성화 상태는 true)
                null                            // completedAt (초기에는 null)
        );
    }

    @Transactional
    public AccountUserResponse getSavingAccount(UUID childId) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 1. 활성 저축통장 먼저 확인
        Optional<SavingAccount> activeAccount = savingAccountRepository.findActiveSavingAccount(child);

        if (activeAccount.isPresent()) {
            SavingAccount account = activeAccount.get();

            // 만료 체크
            if (account.getEndDate().isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")))) {
                // 즉시 만료 처리
                child.setPoint(child.getPoint() + account.getAccountPoint());
                account.setActive(false);
                account.setSuccess(false);
                account.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // ✅ 완료 시점 저장

                savingAccountRepository.save(account);
                userRepository.save(child);
                savingAccountHistoryService.logAccountExpired(account);

                log.info("⏰ 조회 시 만료 처리 - 저축금 {}P 반환, childId: {}",
                        account.getAccountPoint(), childId);

                // 만료 처리 후 정보 반환
                return buildAccountResponse(account, child, "EXPIRED", account.getAccountPoint());
            }

            // 정상 활성 계좌
            return buildAccountResponse(account, child, "ACTIVE", null);
        }

        // 2. 활성 저축통장 없으면 → 가장 최근 완료된 저축통장 조회
        // User 객체로 호출
        Optional<SavingAccount> latestAccount = savingAccountRepository.findFirstByChildAndActiveFalseAndCompletedAtIsNotNullOrderByCompletedAtDesc(child);
        if (latestAccount.isPresent()) {
            SavingAccount account = latestAccount.get();

            // 비활성 상태이므로 이미 달성/만료 처리됨
            if (account.getSuccess() != null && account.getSuccess()) {
                // 목표 달성
                Integer rewardPoint = account.getRewardPoint() != null ? account.getRewardPoint() : 0;
                Integer totalReward = account.getAccountPoint() + rewardPoint;
                return buildAccountResponse(account, child, "ACHIEVED", totalReward);
            } else {
                // 만료됨
                Integer accountPoint = account.getAccountPoint() != null ? account.getAccountPoint() : 0;
                return buildAccountResponse(account, child, "EXPIRED", accountPoint);
            }
        }

        // 3. 저축통장 아예 없음
        return AccountUserResponse.builder()
                .status("NONE")
                .currentPoint(child.getPoint())
                .build();
    }

    // ✅ 중복 코드 제거를 위한 헬퍼 메서드
    private AccountUserResponse buildAccountResponse(SavingAccount account, User child, String status, Integer returnedPoints) {
        AccountUserResponse.AccountUserResponseBuilder builder = AccountUserResponse.builder()
                .createdDate(account.getCreatedDate())
                .endDate(account.getEndDate())
                .goalAmount(account.getGoalAmount())
                .accountPoint(account.getAccountPoint())
                .currentPoint(child.getPoint())
                .status(status);

        if (returnedPoints != null) {
            builder.returnedPoints(returnedPoints);
        }

        return builder.build();
    }

    @Transactional
    public DailyDepositResponse dailyDeposit(UUID childId, DailyDepositRequest request) {
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        SavingAccount savingAccount = savingAccountRepository.findActiveSavingAccount(child)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 저축 통장이 존재하지 않습니다."));

        // ✅ 마감일 체크 및 비활성화 처리 - 저축금만 돌려줌
        if (savingAccount.getEndDate().isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")))) {
            savingAccount.setActive(false);
            savingAccount.setSuccess(false);
            savingAccount.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // ✅ 완료 시점 저장

            // ✅ 수정: 저축금만 돌려줌 (보상 없음)
            child.setPoint(child.getPoint() + savingAccount.getAccountPoint());

            savingAccountRepository.save(savingAccount);
            userRepository.save(child);
            savingAccountHistoryService.logAccountExpired(savingAccount);

            log.info("⏰ 기한 만료! 저축금 {}P만 반환 - childId: {}",
                    savingAccount.getAccountPoint(), childId);

            throw new IllegalArgumentException("저축 통장이 만료되었습니다.");
        }

        // ✅ 포인트 부족 체크
        if (child.getPoint() < request.getDepositPoint()) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        // ✅ 입금 처리 (객체 직접 업데이트)
        processDeposit(child, savingAccount, request);

        // ✅ 이제 savingAccount 객체가 정확한 accountPoint를 가지고 있음
        savingAccountHistoryService.logDeposit(savingAccount, request.getDepositPoint(), savingAccount.getAccountPoint());

        // 목표 달성 체크
        if(request.getSuccess() != null && request.getSuccess()) {
            savingAccount.setSuccess(true);
            savingAccount.setActive(false);
            savingAccount.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // ✅ 완료 시점 저장

            Integer totalReward = savingAccount.getAccountPoint() + savingAccount.getRewardPoint();
            child.setPoint(child.getPoint() + totalReward);

            savingAccountHistoryService.logGoalAchieved(savingAccount);
            log.info("🎉 목표 달성! 저축금: {}P + 보상금: {}P = 총 {}P 지급 - childId: {}",
                    savingAccount.getAccountPoint(),
                    savingAccount.getRewardPoint(),
                    totalReward,
                    childId);
        }

        // 모든 변경사항 저장
        savingAccountRepository.save(savingAccount);
        userRepository.save(child);

        return new DailyDepositResponse(
                String.valueOf(child.getPoint()),
                savingAccount.getAccountPoint()  // ✅ 정확한 저축통장 포인트!
        );
    }

    @Transactional
    public void deactivateExpiredAccounts() {
        List<SavingAccount> expiredAccounts = savingAccountRepository.findExpiredSavingAccounts(LocalDate.now());

        for (SavingAccount account : expiredAccounts) {
            // ✅ 수정: 만료된 계좌의 저축금 돌려주기
            User child = account.getChild();
            child.setPoint(child.getPoint() + account.getAccountPoint());

            account.setActive(false);
            account.setSuccess(false);
            account.setCompletedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // ✅ 완료 시점 저장

            savingAccountRepository.save(account);
            userRepository.save(child); // ✅ 추가: 사용자 포인트 저장
            savingAccountHistoryService.logAccountExpired(account);

            log.info("⏰ 스케줄러 - 기간 만료로 저축금 {}P 반환 및 비활성화 - accountId: {}",
                    account.getAccountPoint(), account.getSavingAccountId());
        }
    }

    // ✅ 입금 처리 메서드
    private void processDeposit(User child, SavingAccount savingAccount, DailyDepositRequest request) {
        // 1. 사용자 포인트에서 차감
        child.setPoint(child.getPoint() - request.getDepositPoint());

        // 2. ✅ 저축통장 객체도 직접 업데이트 (DB 쿼리 대신)
        Integer newAccountPoint = savingAccount.getAccountPoint() + request.getDepositPoint();
        savingAccount.setAccountPoint(newAccountPoint);

        // 3. JPA가 변경 감지해서 자동 업데이트됨 (save 호출 시)
        log.info("💰 입금 처리 완료 - 입금액: {}, 누적금액: {} → {}",
                request.getDepositPoint(),
                savingAccount.getAccountPoint() - request.getDepositPoint(),
                savingAccount.getAccountPoint());
    }

}