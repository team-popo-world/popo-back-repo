package com.popoworld.backend.savingAccount.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DailyDepositResponse {
    private String currentPoint; //사용자 잔여 포인트
    private Integer accountPoint; //저축통장 누적 포인트
}
