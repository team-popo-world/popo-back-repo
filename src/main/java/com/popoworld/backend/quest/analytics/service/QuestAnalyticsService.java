package com.popoworld.backend.quest.analytics.service;


import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestAnalyticsService {
    private final UserRepository userRepository;

    //권한 검증 메서드
    public boolean hasAnalyticsAccess(UUID userId,UUID childId){
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 부모인 경우만 - 자녀 관계 확인
            if ("Parent".equals(user.getRole())) {
                List<User> children = userRepository.findAllChildrenByParentId(userId);
                return children.stream().anyMatch(child -> child.getUserId().equals(childId));
            }

            return false; //자녀는 본인 데이터 못봄
    }
}
