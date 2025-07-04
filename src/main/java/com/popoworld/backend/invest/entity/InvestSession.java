package com.popoworld.backend.invest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invest_session")
public class InvestSession {
    @Id
    private UUID investSessionId;

    private UUID childId;

    private String chapterId;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private Boolean success;

    private Integer profit;

    private Integer clusterNum;

    private UUID scenarioId;
}
