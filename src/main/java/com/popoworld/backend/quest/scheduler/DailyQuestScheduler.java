package com.popoworld.backend.quest.scheduler;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestLabel;
import com.popoworld.backend.quest.enums.QuestState;
import com.popoworld.backend.quest.repository.QuestRepository;
import com.popoworld.backend.quest.service.QuestHistoryService;
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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DailyQuestScheduler {

    private final QuestRepository questRepository;
    private final UserRepository childRepository;
    private final QuestHistoryService questHistoryService;

    /**
     * 매일 자정에 일일퀘스트만 리셋
     * 부모퀘스트는 실시간 처리로 변경
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 매일 자정
    @Transactional
    public void resetDailyQuests() {
        log.info("🎮 일일퀘스트 리셋 시작 - {}", LocalDateTime.now());

        try {
            // 1단계: 모든 일일퀘스트 삭제
            questRepository.deleteByType(Quest.QuestType.DAILY);
            log.info("🗑️ 기존 일일퀘스트 모두 삭제 완료");

            // 2단계: 모든 아이들 목록 조회
            List<UUID> allChildren = getAllChildren();
            log.info("📊 전체 아이 수: {}", allChildren.size());

            // 3단계: 각 아이에게 새로운 일일퀘스트 생성
            int totalCreated = 0;
            for (UUID childId : allChildren) {
                List<Quest> newQuests = createDailyQuestsForChild(childId);
                questRepository.saveAll(newQuests);
                // 이렇게 수정해야 함
                newQuests.forEach(quest -> questHistoryService.logQuest(quest));
                totalCreated += newQuests.size();
                log.info("✅ 아이 [{}]에게 일일퀘스트 {}개 생성", childId, newQuests.size());
            }

            log.info("✅ 일일퀘스트 리셋 완료 - 총 {}개 퀘스트 생성", totalCreated);

        } catch (Exception e) {
            log.error("❌ 일일퀘스트 리셋 실패", e);
        }
    }

    /**
     * 선택사항: 하루에 한 번 부모퀘스트 정리 (보험용)
     * 실시간 처리에서 놓친 것들을 위한 백업 처리
     */
    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Seoul") // 매일 새벽 0시 30분
    @Transactional
    public void cleanupExpiredParentQuests() {
        log.info("🧹 부모퀘스트 정리 작업 시작");

        LocalDateTime nowKST = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        List<Quest> expiredQuests = questRepository.findAllExpirableParentQuests(
                Quest.QuestType.PARENT,
                nowKST
        );

        for (Quest quest : expiredQuests) {
            quest.changeState(QuestState.EXPIRED);
            log.info("🧹 정리 작업으로 만료 처리: {}", quest.getName());
        }

        if (!expiredQuests.isEmpty()) {
            questRepository.saveAll(expiredQuests);
            log.info("🧹 부모퀘스트 정리 완료: {}개", expiredQuests.size());
        } else {
            log.info("🧹 정리할 만료 퀘스트 없음");
        }
    }

    /**
     * 특정 아이에게 일일퀘스트 5개 생성
     */
    private List<UUID> getAllChildren() {
        List<User> users = childRepository.findByRole("Child");
        return users.stream()
                .map(User::getUserId)
                .collect(Collectors.toList());
    }

    /**
     * 특정 아이에게 일일퀘스트 5개 생성
     */
    private List<Quest> createDailyQuestsForChild(UUID childId) {
        List<Quest> dailyQuests = new ArrayList<>();

        // 🔥 일일 퀘스트에 적절한 라벨 추가
        dailyQuests.add(Quest.createDailyQuest(childId, "양치하기", "밥 먹었으면 포포와 양치하자!", 100, QuestLabel.HABIT));
        dailyQuests.add(Quest.createDailyQuest(childId, "장난감 정리하기", "가지고 온 장난감은 스스로 치워볼까?", 100, QuestLabel.HOUSEHOLD));
        dailyQuests.add(Quest.createDailyQuest(childId, "이불 개기", "일어나면 이불을 예쁘게 개자!", 100, QuestLabel.HABIT));
        dailyQuests.add(Quest.createDailyQuest(childId, "식탁 정리 도와주기", "먹고 난 그릇, 포포랑 정리해보자!", 100, QuestLabel.HOUSEHOLD));
        dailyQuests.add(Quest.createDailyQuest(childId, "하루 이야기 나누기", "오늘 어땠는지 부모님과 얘기해보자!", 100, QuestLabel.HABIT));

        return dailyQuests;
    }
}