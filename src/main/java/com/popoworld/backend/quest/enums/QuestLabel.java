package com.popoworld.backend.quest.enums;

public enum QuestLabel {
    HABIT("생활습관"),
    STUDY("학습"),
    HOUSEHOLD("집안일"),
    ERRAND("심부름"),
    POPO("포포월드 기능"),
    ETC("기타");

    private final String description;

    QuestLabel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
