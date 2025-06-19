package com.popoworld.backend.market.entity;

public enum ProductStatus {
    REGISTERED("등록됨"),      // 상점에서 구매 가능
    PURCHASED("구매됨"),       // 구매된 상품 (기존 용도)
    DISCONTINUED("단종됨");    // 상점에서 구매 불가, 인벤토리에서는 사용 가능

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
