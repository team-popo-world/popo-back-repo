package com.popoworld.backend.savingAccount.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DailyDepositRequest {
    private Integer depositPoint;
    private Boolean success;
}
