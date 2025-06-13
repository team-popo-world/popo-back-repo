package com.popoworld.backend.savingAccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreateRequest {
    private Integer goalAmount;
    private LocalDate createdAt; //시작 날짜
    private LocalDate endDate; //끝 날짜
    private Integer rewardPoint; //목표금액의 10퍼센트의 보상
}
