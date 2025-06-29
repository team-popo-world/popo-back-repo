package com.popoworld.backend.report.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_analysis")
public class ChildReport {
    @Id
    private String id;

    @Field(targetType = FieldType.STRING)
    private UUID userId; // childId 등으로 사용

    private String all;
    private String invest;
    private String quest;
    private String shop;
}
