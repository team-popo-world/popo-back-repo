package com.popoworld.backend.market.dto.parent;

import com.popoworld.backend.market.entity.ProductLabel;
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
    private String productImage;
    private ProductLabel label;
}
