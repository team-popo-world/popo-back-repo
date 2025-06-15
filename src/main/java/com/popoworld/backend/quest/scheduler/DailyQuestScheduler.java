package com.popoworld.backend.quest.scheduler;

import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestState;
import com.popoworld.backend.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyQuestScheduler {

    private final QuestRepository questRepository;
    private final UserRepository childRepository;


    /**
     * 매일 자정에 일일퀘스트 리셋 및 부모퀘스트 만료 처리
     */
    // 매일 새벽 5시에 실행
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    @Transactional
    public void dailyMaintenance() {
        log.info("🎮 일일 유지보수 시작 - {}", LocalDateTime.now());

        try {
            // 1단계: 부모퀘스트 만료 처리 (먼저 처리)
            expireOverdueParentQuests();

            // 2단계: 모든 일일퀘스트 삭제
            questRepository.deleteByType(Quest.QuestType.DAILY);
            log.info("🗑️ 기존 일일퀘스트 모두 삭제 완료");

            // 3단계: 모든 아이들 목록 조회 (🔥 하드코딩된 목록)
            List<UUID> allChildren = getAllChildren();
            log.info("📊 전체 아이 수: {}", allChildren.size());

            // 4단계: 각 아이에게 새로운 일일퀘스트 생성
            int totalCreated = 0;
            for (UUID childId : allChildren) {
                List<Quest> newQuests = createDailyQuestsForChild(childId);
                questRepository.saveAll(newQuests);
                totalCreated += newQuests.size();
                log.info("✅ 아이 [{}]에게 퀘스트 {}개 생성", childId, newQuests.size());
            }

            log.info("✅ 일일 유지보수 완료 - 총 {}개 퀘스트 생성", totalCreated);

        } catch (Exception e) {
            log.error("❌ 일일 유지보수 실패", e);
        }
    }

    /**
     * 부모퀘스트 만료 처리 (쿼리 방식) - 새로 추가된 메서드
     */
    @Transactional
    public void expireOverdueParentQuests() {
        log.info("⏰ 부모퀘스트 만료 처리 시작");

        // 한국 시간으로 비교
        LocalDateTime nowKST = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("🕐 현재 한국 시간: {}", nowKST);

        int expiredCount = questRepository.updateExpiredParentQuests(
                nowKST,  // 한국 시간 사용
                Quest.QuestType.PARENT,
                QuestState.EXPIRED,
                QuestState.COMPLETED
        );

        log.info("✅ 부모퀘스트 만료 처리 완료 - {}개 퀘스트 만료", expiredCount);
    }


    /**
     * 특정 아이에게 일일퀘스트 5개 생성
     */
    private List<UUID> getAllChildren() {
        List<UUID> Children = childRepository.findAllChildrenByRole("Child");

        log.info("🧪 하드코딩된 테스트 아이 목록 사용: {}명", Children.size());
        return Children;
    }

    /**
     * 특정 아이에게 일일퀘스트 5개 생성
     */
    private List<Quest> createDailyQuestsForChild(UUID childId) {
        List<Quest> dailyQuests = new ArrayList<>();

        // 🎯 정적 팩토리 메서드 사용
        dailyQuests.add(Quest.createDailyQuest(childId, "양치하기", "밥 먹었으면 포포와 양치하자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "장난감 정리하기", "가지고 온 장난감은 스스로 치워볼까?", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "이불 개기", "일어나면 이불을 예쁘게 개자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "식탁 정리 도와주기", "먹고 난 그릇, 포포랑 정리해보자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "하루 이야기 나누기", "오늘 어땠는지 부모님과 얘기해보자!", 100));

        return dailyQuests;
    }
}