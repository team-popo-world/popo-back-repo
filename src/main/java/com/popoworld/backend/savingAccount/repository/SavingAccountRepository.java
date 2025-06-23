package com.popoworld.backend.savingAccount.repository;

import com.popoworld.backend.User.User;
import com.popoworld.backend.savingAccount.entity.SavingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingAccountRepository extends JpaRepository<SavingAccount,UUID> {
    //활성 저축통장 존재 여부 확인
    @Query("SELECT COUNT(s) > 0 FROM SavingAccount s WHERE s.child = :child AND s.active = true")
    boolean existsActiveSavingAccount(@Param("child") User child);

    //활성 저축통장 조회
    @Query("SELECT s FROM SavingAccount s WHERE s.child = :child AND s.active = true")
    Optional<SavingAccount> findActiveSavingAccount(@Param("child") User child);

    //만료된 저축통장들 조회 (active=true이면서 endDate 지난 것들)
    @Query("SELECT s FROM SavingAccount s WHERE s.endDate < :currentDate AND s.active = true")
    List<SavingAccount> findExpiredSavingAccounts(@Param("currentDate") LocalDate currentDate);

    // 저축통장 포인트 업데이트
    @Modifying
    @Query("UPDATE SavingAccount s SET s.accountPoint = s.accountPoint + :depositPoint WHERE s.savingAccountId = :accountId")
    void updateAccountPoint(@Param("accountId") UUID accountId, @Param("depositPoint") Integer depositPoint);

    //success 상태 업데이트
    @Modifying
    @Query("UPDATE SavingAccount s SET s.success = :success WHERE s.savingAccountId = :accountId")
    void updateSuccess(@Param("accountId") UUID accountId, @Param("success") Boolean success);

    //저축통장 비활성화 (active를 false로 설정)
    @Modifying
    @Query("UPDATE SavingAccount s SET s.active = false WHERE s.savingAccountId = :accountId")
    void deactivateSavingAccount(@Param("accountId") UUID accountId);

    // SavingAccountRepository.java에 추가
    @Query("SELECT s FROM SavingAccount s WHERE s.child = :child AND s.active = false AND s.endDate >= :recentDate ORDER BY s.endDate DESC")
    Optional<SavingAccount> findRecentFinishedAccount(@Param("child") User child, @Param("recentDate") LocalDate recentDate);

    // 기존 쿼리 삭제하고 이걸로 교체
    Optional<SavingAccount> findFirstByChildAndActiveFalseAndCompletedAtIsNotNullOrderByCompletedAtDesc(User child);
    // 자녀의 모든 저축통장 조회 (최신순: 종료일 기준 내림차순)
    List<SavingAccount> findAllByChildOrderByEndDateDesc(User child);

    @Query("SELECT s FROM SavingAccount s WHERE s.child = :child " +
            "ORDER BY s.active DESC, s.endDate DESC")
    List<SavingAccount> findAllByChildOrderByActiveAndEndDate(@Param("child") User child);
}
