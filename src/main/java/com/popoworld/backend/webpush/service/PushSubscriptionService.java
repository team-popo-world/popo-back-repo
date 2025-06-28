package com.popoworld.backend.webpush.service;

import com.popoworld.backend.webpush.dto.PushSubscriptionRequest;
import com.popoworld.backend.webpush.entity.WebPush;
import com.popoworld.backend.webpush.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushRepository repository;

    /**
     * 구독 정보 저장. 같은 userId가 있으면 덮어쓰기.
     */
    public void save(UUID userId, PushSubscriptionRequest request) {
        Optional<WebPush> existing = repository.findByUserId(userId);

        WebPush subscription = existing.orElseGet(WebPush::new);
        subscription.setUserId(userId);
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getP256dh());
        subscription.setAuth(request.getAuth());

        repository.save(subscription);
    }

    /**
     * userId로 구독 정보 조회
     */
    public WebPush getByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 구독 정보가 없습니다."));
    }
}