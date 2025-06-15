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
public class AccountUserResponse {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private Integer goalAmount;
    private Integer accountPoint;
    private Integer currentPoint;
    private String status; //"ACTIVE", "EXPIRED", "ACHIEVED","NONE"
    private Integer returnedPoints; //반환된 포인트(만료/달성시)
}
