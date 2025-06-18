package com.popoworld.backend.market.dto.child;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PurchaseItemResponse {
    private int currentPoint;
    private int purchasedAmount; //구매수량
    private int totalPrice;


}
