package com.popoworld.backend.market.dto.child;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UseItemResponse {
    private String message;
    private int remainingStock; //사용 후 남은 수량
}
