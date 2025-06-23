package com.popoworld.backend.savingAccount.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.savingAccount.dto.ParentDepositHistoryResponse;
import com.popoworld.backend.savingAccount.dto.ParentSavingAccountResponse;
import com.popoworld.backend.savingAccount.entity.SavingAccount;
import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import com.popoworld.backend.savingAccount.repository.SavingAccountHistoryMongoRepository;
import com.popoworld.backend.savingAccount.repository.SavingAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentSavingService {
    private final SavingAccountHistoryMongoRepository savingAccountHistoryMongoRepository;
    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;

    /**
     * 저축 통장 현황 조회 (왼쪽 화면)
     */
    public ParentSavingAccountResponse getChildSavingAccount(UUID childId) {
        // 1. 자녀 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자녀입니다."));

        // 2. 현재 저축통장 조회
        Optional<SavingAccount> currentAccount = savingAccountRepository.findActiveSavingAccount(child);

        if (currentAccount.isEmpty()) {
            // 저축통장이 없는 경우
            return ParentSavingAccountResponse.builder()
                    .status("NONE")
                    .goalAmount(0)
                    .currentAccountPoint(0)
                    .currentPercent(0)
                    .totalDepositCount(0)
                    .build();
        }

        SavingAccount account = currentAccount.get();

        // 3. 입금 횟수 계산 (MongoDB에서)
        List<SavingAccountHistory> deposits = savingAccountHistoryMongoRepository
                .findAllDepositsByChildIdOrderByTimestampDesc(childId);

        // 4. 달성률 계산
        int currentPercent = calculatePercent(account.getAccountPoint(), account.getGoalAmount());

        return ParentSavingAccountResponse.builder()
                .goalAmount(account.getGoalAmount())
                .currentAccountPoint(account.getAccountPoint())
                .currentPercent(currentPercent)
                .createdDate(account.getCreatedDate())
                .endDate(account.getEndDate())
                .status("ACTIVE")
                .totalDepositCount(deposits.size())
                .build();
    }

    /**
     * 저축 내역 전체 조회 (오른쪽 화면)
     */
    public List<ParentDepositHistoryResponse> getChildDepositHistory(UUID childId) {
        // 1. 자녀 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자녀입니다."));

        // 2. MongoDB에서 모든 입금 내역 조회
        List<SavingAccountHistory> allDeposits = savingAccountHistoryMongoRepository
                .findAllDepositsByChildIdOrderByTimestampDesc(childId);

        // 3. DTO로 변환
        return allDeposits.stream()
                .map(history -> convertToDepositResponse(history, child.getName()))
                .collect(Collectors.toList());
    }

    /**
     * SavingAccountHistory -> ParentDepositHistoryResponse 변환
     */
    private ParentDepositHistoryResponse convertToDepositResponse(SavingAccountHistory history, String childName) {
        return ParentDepositHistoryResponse.builder()
                .depositAmount(history.getDailyDepositAmount())
                .depositDate(history.getTimestamp().toLocalDate())  // 날짜만 추출
                .depositTime(history.getTimestamp())                // 전체 시간
                .accountPointAfter(history.getAccountPoint())
                .childName(childName)  // "아마카"
                .profileImage(null)    // 프로필 이미지는 별도 처리 필요
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
