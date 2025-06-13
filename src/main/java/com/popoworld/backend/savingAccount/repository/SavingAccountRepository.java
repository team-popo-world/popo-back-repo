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
    // 특정 사용자의 활성 저축 통장 존재 여부 확인 (마감일이 지나지 않은 것)
    @Query("SELECT COUNT(s) > 0 FROM SavingAccount s WHERE s.child = :child AND s.endDate >= :currentDate")
    boolean existsActiveSavingAccount(@Param("child") User child, @Param("currentDate") LocalDate currentDate);

    // 특정 사용자의 활성 저축 통장 조회 (마감일이 지나지 않은 것)
    @Query("SELECT s FROM SavingAccount s WHERE s.child = :child AND s.endDate >= :currentDate")
    Optional<SavingAccount> findActiveSavingAccount(@Param("child") User child, @Param("currentDate") LocalDate currentDate);
    // 새로운 메서드들
    // 만료된 저축통장들 조회 (마감일이 지났는데 success가 false인 것들)
    @Query("SELECT s FROM SavingAccount s WHERE s.endDate < :currentDate AND s.success = false")
    List<SavingAccount> findExpiredSavingAccounts(@Param("currentDate") LocalDate currentDate);

    // 새로운 메서드들
    // 저축통장 포인트 업데이트
    @Modifying
    @Query("UPDATE SavingAccount s SET s.accountPoint = s.accountPoint + :depositPoint WHERE s.savingAccountId = :accountId")
    void updateAccountPoint(@Param("accountId") UUID accountId, @Param("depositPoint") Integer depositPoint);

    // 저축통장 성공 상태 업데이트
    @Modifying
    @Query("UPDATE SavingAccount s SET s.success = :success WHERE s.savingAccountId = :accountId")
    void updateSuccess(@Param("accountId") UUID accountId, @Param("success") Boolean success);

    // 저축통장 비활성화 (success를 null로 설정)
    @Modifying
    @Query("UPDATE SavingAccount s SET s.success = null WHERE s.savingAccountId = :accountId")
    void deactivateSavingAccount(@Param("accountId") UUID accountId);
}
