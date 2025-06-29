package com.popoworld.backend.webpush.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.dto.ChildInfoDTO;
import com.popoworld.backend.User.dto.Response.ChildLoginResponseDTO;
import com.popoworld.backend.User.dto.Response.ParentLoginResponseDTO;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.webpush.dto.MessageRequestDTO;
import com.popoworld.backend.webpush.dto.PushSubscriptionRequest;
import com.popoworld.backend.webpush.entity.WebPush;
import com.popoworld.backend.webpush.repository.PushRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {

    private final PushRepository repository;
    private final UserRepository userRepository;

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
    public WebPush getByUserId(MessageRequestDTO requestDTO) {
        if ("Parent".equalsIgnoreCase(requestDTO.getRole())) {
            User parent = userRepository.findByParentCodeAndRole(requestDTO.getParentCode(), requestDTO.getRole())
                    .orElseThrow(() -> new IllegalArgumentException("유효한 부모 코드가 아니에요."));
            return repository.findByUserId(parent.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 유저의 구독 정보가 없습니다."));
        } else if ("Child".equalsIgnoreCase(requestDTO.getRole())) {
            return repository.findByUserId(requestDTO.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("자식 유저의 구독 정보가 없습니다."));
        } else {
            throw new IllegalArgumentException("role 값은 'Parent' 또는 'Child'만 가능합니다.");
        }
    }
}