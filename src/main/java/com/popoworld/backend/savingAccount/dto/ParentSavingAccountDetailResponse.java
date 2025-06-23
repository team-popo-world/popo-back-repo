package com.popoworld.backend.savingAccount.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParentSavingAccountDetailResponse {
    private Integer goalAmount;           // 목표 금액
    private Integer currentAccountPoint;  // 현재 저축 금액
    private Integer currentPercent;       // 달성률(%)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;        // 통장 생성일(시작일)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;            // 통장 종료일

    private String status;                // ACTIVE, COMPLETED, EXPIRED
    private Integer totalDepositCount;    // 총 입금 횟수
    private List<DepositDetailResponse> deposits; // 입금 내역(최신순)
}
