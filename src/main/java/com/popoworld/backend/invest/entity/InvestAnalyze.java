package com.popoworld.backend.invest.entity;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Document(collection = "invest_analyze")
public class InvestAnalyze {
    @Id
    @Field(targetType = FieldType.STRING)
    private UUID id;

    @Field(targetType = FieldType.STRING)
    private UUID userId;

    private String graphType;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
}
