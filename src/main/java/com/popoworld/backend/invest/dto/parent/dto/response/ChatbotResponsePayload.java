package com.popoworld.backend.invest.dto.parent.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ChatbotResponsePayload {
    private UUID userId;
    private UUID requestId;
    private String story;

}
