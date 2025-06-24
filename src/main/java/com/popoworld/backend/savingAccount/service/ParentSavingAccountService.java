package com.popoworld.backend.savingAccount.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.savingAccount.dto.DepositDetailResponse;
import com.popoworld.backend.savingAccount.dto.ParentSavingAccountDetailResponse;
import com.popoworld.backend.savingAccount.entity.SavingAccount;
import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import com.popoworld.backend.savingAccount.repository.SavingAccountHistoryMongoRepository;
import com.popoworld.backend.savingAccount.repository.SavingAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentSavingAccountService {

    private final SavingAccountHistoryMongoRepository savingAccountHistoryMongoRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;

    /**
     * 자녀의 모든 저축통장 조회 (활성 > 비활성, 날짜순)
     */
    public List<ParentSavingAccountDetailResponse> getAllChildSavingAccounts(UUID childId) {
        // 1. 자녀 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자녀입니다."));

        // 2. 자녀의 모든 저축통장 조회
        List<SavingAccount> allAccounts = savingAccountRepository.findAllByChildOrderByActiveAndEndDate(child);

        if (allAccounts.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 🆕 Service에서 추가 정렬 (더 세밀한 제어)
        List<SavingAccount> sortedAccounts = allAccounts.stream()
                .sorted(this::compareAccounts)
                .collect(Collectors.toList());

        // 4. 각 저축통장별로 상세 정보 + 입금내역 조합
        return sortedAccounts.stream()
                .map(account -> buildAccountDetailResponse(account, child))
                .collect(Collectors.toList());
    }

    /**
     * 🆕 저축통장 정렬 비교 함수
     * 1순위: 활성 상태 (active = true가 먼저)
     * 2순위: 종료일 (최신이 먼저)
     */
    private int compareAccounts(SavingAccount a1, SavingAccount a2) {
        // 1순위: 활성 상태 비교 (true > false)
        if (a1.getActive() != a2.getActive()) {
            return Boolean.compare(a2.getActive(), a1.getActive()); // true가 먼저
        }

        // 2순위: 종료일 비교 (최신이 먼저)
        return a2.getEndDate().compareTo(a1.getEndDate()); // DESC
    }

    /**
     * 저축통장 1개의 상세 정보 생성 (입금내역 포함)
     */
    private ParentSavingAccountDetailResponse buildAccountDetailResponse(SavingAccount account, User child) {
        // 1. 해당 저축통장의 모든 입금내역 조회
        List<SavingAccountHistory> accountDeposits = getDepositsByAccount(account);

        log.info("🔍 저축통장 ID: {}, 활성: {}, 종료일: {}, 입금내역: {}건",
                account.getSavingAccountId(),
                account.getActive(),
                account.getEndDate(),
                accountDeposits.size());

        // 2. 입금내역을 DTO로 변환 (최신순)
        List<DepositDetailResponse> depositResponses = accountDeposits.stream()
                .map(history -> convertToDepositDetail(history, child.getName()))
                .collect(Collectors.toList());

        // 3. 현재 달성률 계산
        int currentPercent = calculatePercent(account.getAccountPoint(), account.getGoalAmount());

        // 4. 상태 결정
        String status = determineAccountStatus(account);

        return ParentSavingAccountDetailResponse.builder()
                .goalAmount(account.getGoalAmount())
                .currentAccountPoint(account.getAccountPoint())
                .currentPercent(currentPercent)
                .createdDate(account.getCreatedDate())
                .endDate(account.getEndDate())
                .status(status)
                .totalDepositCount(accountDeposits.size())
                .deposits(depositResponses)
                .build();
    }

    /**
     * 특정 저축통장의 입금내역만 조회
     */
    private List<SavingAccountHistory> getDepositsByAccount(SavingAccount account) {
        // savingAccountId로 해당 통장의 입금내역만 조회
        return savingAccountHistoryMongoRepository.findDepositsBySavingAccountId(
                account.getSavingAccountId()
        );
    }

    /**
     * 저축통장 상태 결정
     */
    private String determineAccountStatus(SavingAccount account) {
        if (account.getActive()) {
            // 활성 계좌인데 만료일 지났으면 만료 처리 필요
            if (account.getEndDate().isBefore(LocalDate.now())) {
                return "EXPIRED";
            }
            return "ACTIVE";
        } else {
            // 비활성 계좌
            if (account.getSuccess() != null && account.getSuccess()) {
                return "COMPLETED"; // 목표 달성 완료
            } else {
                return "EXPIRED";   // 기간 만료
            }
        }
    }

    /**
     * SavingAccountHistory -> DepositDetailResponse 변환
     */
    private DepositDetailResponse convertToDepositDetail(SavingAccountHistory history, String childName) {
        return DepositDetailResponse.builder()
                .depositAmount(history.getDailyDepositAmount())
                .depositDate(history.getTimestamp().toLocalDate())
                .depositTime(history.getTimestamp())
                .accountPointAfter(history.getAccountPoint())
                .childName(childName)
                .profileImage(null)
                .build();
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
}