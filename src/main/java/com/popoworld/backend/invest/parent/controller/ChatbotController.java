package com.popoworld.backend.invest.parent.controller;

import com.popoworld.backend.invest.parent.dto.ChatbotRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
@Tag(name="Chatbot", description = "시나리오 업데이트 챗봇 API")
public class ChatbotController {

//    @Operation(summary = "채팅 입력", description = "채팅으로 시나리오 업데이트 요청")
//    @PostMapping("/input")
//    public ResponseEntity<?> chat(@RequestBody ChatbotRequestDTO) {
//
//    }
}
