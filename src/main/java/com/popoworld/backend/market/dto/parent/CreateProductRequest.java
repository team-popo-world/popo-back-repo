package com.popoworld.backend.market.dto.parent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductRequest {
    private UUID childId;
    private String productName;
    private int productPrice;
    private int productStock;
    private String productImage;

}
