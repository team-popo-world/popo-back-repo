package com.popoworld.backend.market.entity;


public enum ProductLabel {
    FOOD("먹이"),
    SNACK("간식"),
    ENTERTAINMENT("오락"),
    TOY("장난감"),
    EDUCATION("교육 및 문구"),
    ETC("기타");
    private final String description;

    ProductLabel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
