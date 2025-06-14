package com.popoworld.backend.invest.dto.parent.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatKafkaPayload {
    private UUID userId;
    private String chapterId;
    private String story;
    private String editRequest;
}