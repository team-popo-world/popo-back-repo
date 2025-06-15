package com.popoworld.backend.savingAccount.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "saving_account_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class SavingAccountHistory {
    @Id
    private UUID id;
    private UUID childId;
    private Integer accountPoint; //저축통장 포인트
    private Integer goalAmount; //목표 금액
    private Integer rewardPoint; //보상 포인트
    private Integer dailyDepositAmount; //일일 입금액
    private LocalDate createdDate; //생성일
    private LocalDate endDate; //종료일
    private Boolean success; //성공 여부
    private String eventType; // 생성, 입금, 성공, 만료
    private LocalDateTime timestamp; //타임스탬프
    private Integer percent; //달성률 (0-100)
    private Boolean active; //활성화 여부
}