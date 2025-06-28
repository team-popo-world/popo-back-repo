package com.popoworld.backend.webpush.controller;

import com.popoworld.backend.webpush.dto.PushSubscriptionRequest;
import com.popoworld.backend.webpush.entity.WebPush;
import com.popoworld.backend.webpush.service.PushSubService;
import com.popoworld.backend.webpush.service.PushSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
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

    @GetMapping("/test-alert")
    public void testSend() throws Exception {
        UUID userId = getCurrentUserId();
        WebPush sub = pushSubscriptionService.getByUserId(userId);
        pushService.sendNotification(sub, "🎉 테스트 알림입니다!");
    }
}
