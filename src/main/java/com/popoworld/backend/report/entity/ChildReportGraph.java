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
@Document(collection = "child_report_graph")
public class ChildReportGraph {
    @Id
    @Field(targetType = FieldType.STRING)
    private UUID childId; // childId 등으로 사용

    private String investGraph;
    private String shopGraph;
    private String questGraph;

}
