package com.popoworld.backend.market.analytics.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketAnalyticsService {
    private final UserRepository userRepository;

    //대시보드 접근 권한 검증
    public boolean hasDashboardAccess(UUID userId, UUID childId){
        try{
            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 부모만 자녀 대시보드 조회 가능
            if ("Parent".equals(user.getRole())) {
                List<User> children = userRepository.findAllChildrenByParentId(userId);
                return children.stream().anyMatch(child -> child.getUserId().equals(childId));
            }
            return false; //자녀는 본인 대시보드도 볼 수 없음
        }catch (Exception e){
            log.error("대시보드 권한 검증 중 오류 발생");
            return false;
        }

    }
}
