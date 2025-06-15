package com.popoworld.backend.invest.dto.parent.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ChatbotStoryRequestDTO {
    private String chapterId;
    private String story;
    private String editRequest;
}
