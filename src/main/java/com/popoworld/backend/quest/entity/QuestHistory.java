package com.popoworld.backend.quest.entity;

import com.popoworld.backend.quest.enums.QuestLabel;
import com.popoworld.backend.quest.enums.QuestState;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quest_history")
public class QuestHistory {
    @Id
    @Field(targetType = FieldType.STRING)
    private UUID id;
    @Field(targetType = FieldType.STRING)
    private UUID questId;
    @Field(targetType = FieldType.STRING)
    private UUID childId;
    private String questType;
    private String questName;           // 퀘스트 이름
    private String questDescription;    // 퀘스트 설명
    private QuestState currentState; //현재 상태
    private Integer rewardPoint;
    private LocalDateTime actionTime; //액션 발생 시간
    private QuestLabel label;
}
