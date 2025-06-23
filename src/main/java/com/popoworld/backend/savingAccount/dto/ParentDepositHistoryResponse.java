package com.popoworld.backend.savingAccount.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParentDepositHistoryResponse {
    private Integer depositAmount;        // 입금액 (2,100)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate depositDate;        // 입금 날짜 (7월 19일)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime depositTime;    // 정확한 입금 시간

    private Integer accountPointAfter;    // 입금 후 저축통장 누적액
    private String childName;             // 자녀 이름 (아마카)
    private String profileImage;          // 아바타 이미지 (선택적)
}