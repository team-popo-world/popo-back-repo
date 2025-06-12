package com.popoworld.backend.invest.dto.parent.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatbotRequestDTO {
    private String accessToken;
    private String updateRequest; // 변경 요청 텍스트
}
