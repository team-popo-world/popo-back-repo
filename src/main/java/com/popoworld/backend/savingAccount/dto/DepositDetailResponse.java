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
public class DepositDetailResponse {
    private Integer depositAmount;        // 입금 금액

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate depositDate;        // 입금 날짜

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime depositTime;    // 입금 시각

    private Integer accountPointAfter;    // 입금 후 잔액
    private String childName;             // 자녀 이름
    private String profileImage;          // 프로필 이미지 (없으면 null)
}
