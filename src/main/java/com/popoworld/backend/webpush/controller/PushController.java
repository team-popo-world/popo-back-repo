package com.popoworld.backend.webpush.controller;

import com.popoworld.backend.webpush.dto.MessageRequestDTO;
import com.popoworld.backend.webpush.dto.PushSubscriptionRequest;
import com.popoworld.backend.webpush.entity.WebPush;
import com.popoworld.backend.webpush.service.PushSubService;
import com.popoworld.backend.webpush.service.PushSubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
@Tag(name = "Push Notification" ,description = "알림")
public class PushController {

    private final PushSubscriptionService pushSubscriptionService;
    private final PushSubService pushService;

    @Value("${push.public-key}")
    private String publicKey;

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(publicKey);
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestBody PushSubscriptionRequest request) {
        UUID userId = getCurrentUserId();
        pushSubscriptionService.save(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/message")
    public void testSend(@RequestBody MessageRequestDTO requestDTO) throws Exception {
        WebPush sub = pushSubscriptionService.getByUserId(requestDTO);
        pushService.sendNotification(sub, requestDTO.getMessage());
    }
}
