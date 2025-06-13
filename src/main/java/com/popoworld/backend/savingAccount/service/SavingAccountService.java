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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingAccountService {
    private final SavingAccountRepository savingAccountRepository;
    private final UserRepository userRepository;
    public String createSavingAccount(UUID childId,AccountCreateRequest request){
        User child = userRepository.findById(childId).orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //2. 저축 통장 조회
        if(savingAccountRepository.existsActiveSavingAccount(child, LocalDate.now())){
            throw new IllegalArgumentException("이미 활성화된 저축 통장이 있습니다.");
        }
        SavingAccount savingAccount = buildSaveAccountEntity(request,child);

        savingAccountRepository.save(savingAccount);

        return "저축 통장이 성공적으로 생성되었습니다.";
    }

    private SavingAccount buildSaveAccountEntity(AccountCreateRequest request, User child){
        return new SavingAccount(
          null, //UUID는 @GeneratedValue로 자동 생성
          child,
                0, //초기 계좌 포인트는 0
                request.getGoalAmount(),
                request.getRewardPoint(),
                request.getCreatedAt(),
                request.getEndDate(),
                false //성공 여부는 false
        );
    }

    @Transactional
    public AccountUserResponse getSavingAccount(UUID childId){
        //1. 사용자 존재 확인
        User child = userRepository.findById(childId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 아이디입니다."));

        //2. 활성 저축 통장 조회(마감일이 지나지 않은 것)
        SavingAccount savingAccount = savingAccountRepository.findActiveSavingAccount(child,LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("활성화된 저축 통장이 존재하지 않습니다."));

        //3. 응답 DTO 생성
        return new AccountUserResponse(
                savingAccount.getCreatedDate(),
                savingAccount.getEndDate(),
                savingAccount.getGoalAmount(),
                savingAccount.getAccountPoint(),
                child.getPoint()
        );
    }


    //새로운 매일입금 메서드
    @Transactional
    public DailyDepositResponse dailyDeposit(UUID childId, DailyDepositRequest request) {
        // 1. 사용자 존재 확인
        User child = userRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        //2. 활성 저축통장 조회
        SavingAccount savingAccount = savingAccountRepository.findActiveSavingAccount(child, LocalDate.now())
                .orElseThrow(() -> new IllegalArgumentException("활성화된 저축 통장이 존재하지 않습니다."));

        // 3. 마감일 체크 및 비활성화 처리
        if (savingAccount.getEndDate().isBefore(LocalDate.now())) {
            savingAccountRepository.deactivateSavingAccount(savingAccount.getSavingAccountId());
            throw new IllegalArgumentException("저축 통장이 만료되었습니다.");
        }

        //4. 입금 처리(업데이트)
        processDeposit(child,savingAccount,request);

        //5. 목표 달성 체크 및 success 업데이트
        if(request.getSuccess()!=null && request.getSuccess()){
            savingAccountRepository.updateSuccess(savingAccount.getSavingAccountId(),true);
            //보상 포인트 지급
            child.setPoint(child.getPoint()+ savingAccount.getRewardPoint());
        }

        //6. 사용자 정보 저장
        userRepository.save(child);

        // 7. 업데이트된 저축통장 정보 다시 조회
        SavingAccount updatedAccount = savingAccountRepository.findById(savingAccount.getSavingAccountId())
                .orElseThrow(() -> new IllegalArgumentException("저축 통장 정보를 찾을 수 없습니다."));

        // 8. 응답 생성
        return new DailyDepositResponse(
                String.valueOf(child.getPoint()), // currentPoint를 String으로
                updatedAccount.getAccountPoint()   // accountPoint
        );
    }
    // 만료된 저축통장들 일괄 비활성화 (스케줄러에서 호출 가능)
    @Transactional
    public void deactivateExpiredAccounts() {
        List<SavingAccount> expiredAccounts = savingAccountRepository.findExpiredSavingAccounts(LocalDate.now());

        for (SavingAccount account : expiredAccounts) {
            savingAccountRepository.deactivateSavingAccount(account.getSavingAccountId());
        }
    }

    private void processDeposit(User child, SavingAccount savingAccount, DailyDepositRequest request) {
        // 사용자 포인트에서 차감
        child.setPoint(child.getPoint() - request.getDepositPoint());

        // 저축통장 포인트 직접 업데이트 (UPDATE 쿼리 사용)
        savingAccountRepository.updateAccountPoint(savingAccount.getSavingAccountId(), request.getDepositPoint());
    }

    private void deactivateExpiredAccount(SavingAccount savingAccount) {
        // UPDATE 쿼리로 직접 비활성화
        savingAccountRepository.deactivateSavingAccount(savingAccount.getSavingAccountId());
    }

    }


