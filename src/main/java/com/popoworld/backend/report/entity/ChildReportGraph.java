package com.popoworld.backend.report.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_graph")
public class ChildReportGraph {
    @Id
    private String id;

    @Field(targetType = FieldType.STRING)
    private UUID userId; // childId 등으로 사용

    @Field("invest_graph")
    private List<Map<String, Object>> investGraph;

    @Field("quest_graph")
    private List<Map<String,Object>> questGraph;

    @Field("shop_graph")
    private List<Map<String,Object>> shopGraph;

}
