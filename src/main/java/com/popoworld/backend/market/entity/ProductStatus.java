package com.popoworld.backend.market.entity;

public enum ProductStatus {
    REGISTERED("등록됨"),      // 상점에서 구매 가능
    PURCHASED("구매됨"),       // 부모 상품이 구매되어 인벤토리에 있음
    USED("사용됨"),           // 부모 상품이 사용됨 (승인 대기)
    APPROVED("승인됨"),        // 부모 상품 사용이 승인됨
    DISCONTINUED("단종됨");    // 상점에서 구매 불가

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
