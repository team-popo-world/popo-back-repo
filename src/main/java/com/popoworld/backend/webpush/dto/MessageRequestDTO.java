package com.popoworld.backend.webpush.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class MessageRequestDTO {
    private UUID userId;
    private String role; // 메세지 받는 사람 역할
    private String message; // 보낼 메세지
}
