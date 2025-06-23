package com.popoworld.backend.savingAccount.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParentSavingAccountResponse {
    private Integer goalAmount;           // 목표 저축 금액
    private Integer currentAccountPoint;  // 현재 저축 금액
    private Integer currentPercent;       // 목표 달성률 (32%)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;        // 저축 시작일 (2025-06-20)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;            // 저축 마감일 (2025-09-15)

    private String status;                // ACTIVE, ACHIEVED, EXPIRED, NONE
    private Integer totalDepositCount;    // 총 입금 횟수
}
