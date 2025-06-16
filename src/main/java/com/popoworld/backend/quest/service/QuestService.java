package com.popoworld.backend.quest.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.quest.dto.*;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.enums.QuestState;
import com.popoworld.backend.quest.repository.QuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestService {
    private final QuestRepository questRepository;
    private final UserRepository userRepository;
    private final QuestHistoryService questHistoryService;

    // 🎯 메인 메서드: 퀘스트 목록 + 포인트 (실시간 만료 처리 추가)
    @Transactional
    public QuestListWithPointResponse getQuestsWithPoint(UUID childId, String type) {
        // 1. 부모퀘스트 실시간 만료 처리
        checkAndExpireParentQuests(childId);

        // 2. 퀘스트 목록 조회
        List<Quest> quests;
        if (type != null) {
            Quest.QuestType questType = Quest.QuestType.valueOf(type.toUpperCase());
            quests = questRepository.findByChildIdAndType(childId, questType);
        } else {
            quests = questRepository.findByChildId(childId);
        }

        // 3. 퀘스트 DTO 변환 (포인트 정보 없이)
        List<QuestResponse> questResponses = quests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 4. 사용자 포인트 조회
        Integer currentPoint = getUserPoint(childId);

        // 5. 래퍼 객체로 합쳐서 반환
        return QuestListWithPointResponse.builder()
                .currentPoint(currentPoint)
                .quests(questResponses)
                .build();
    }

    /**
     * 특정 아이의 부모퀘스트 중 만료된 것들을 실시간으로 EXPIRED 처리
     */
    @Transactional
    public void checkAndExpireParentQuests(UUID childId) {
        LocalDateTime nowKST = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 해당 아이의 부모퀘스트 중 만료 대상 조회
        List<Quest> expirableQuests = questRepository.findExpirableParentQuests(
                childId,
                Quest.QuestType.PARENT,
                nowKST
        );

        for (Quest quest : expirableQuests) {
            log.info("⏰ 부모퀘스트 실시간 만료 처리: {} (종료시간: {})",
                    quest.getName(), quest.getEndDate());

            quest.changeState(QuestState.EXPIRED);
            questRepository.save(quest);

            // 만료 로그 전송
            questHistoryService.logQuest(quest);
        }

        if (!expirableQuests.isEmpty()) {
            log.info("✅ 부모퀘스트 실시간 만료 처리 완료: {}개", expirableQuests.size());
        }
    }

    // 🔒 안전한 포인트 조회
    private Integer getUserPoint(UUID childId) {
        try {
            User user = userRepository.findById(childId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Integer point = user.getPoint();
            return point != null ? point : 0;
        } catch (Exception e) {
            log.warn("⚠️ 사용자 포인트 조회 실패 - childId: {}, 기본값 0 반환", childId, e);
            return 0;
        }
    }

    @Transactional
    public void createDailyQuestsForNewChild(UUID childId) {
        List<Quest> newQuests = createDailyQuestsForChild(childId);
        questRepository.saveAll(newQuests);
        //로그 전송
        newQuests.forEach(quest -> questHistoryService.logQuest(quest));
        log.info("🆕 새 아이 일일퀘스트 생성 완료 - childId: {}, 퀘스트: {}개", childId, newQuests.size());
    }

    private List<Quest> createDailyQuestsForChild(UUID childId) {
        List<Quest> dailyQuests = new ArrayList<>();
        dailyQuests.add(Quest.createDailyQuest(childId, "양치하기", "밥 먹었으면 포포와 양치하자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "장난감 정리하기", "가지고 온 장난감은 스스로 치워볼까?", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "이불 개기", "일어나면 이불을 예쁘게 개자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "식탁 정리 도와주기", "먹고 난 그릇, 포포랑 정리해보자!", 100));
        dailyQuests.add(Quest.createDailyQuest(childId, "하루 이야기 나누기", "오늘 어땠는지 부모님과 얘기해보자!", 100));
        return dailyQuests;
    }

    @Transactional
    public QuestResponse createParentQuest(ParentQuestRequest request,UUID parentId) {
        LocalDateTime endDateTime = LocalDateTime.parse(request.getEndDate());
        Quest parentQuest = Quest.createParentQuest(
                request.getChildId(),
                request.getName(),
                request.getDescription(),
                request.getReward(),
                endDateTime,
                request.getImageUrl()
        );
        Quest savedQuest = questRepository.save(parentQuest);

        //퀘스트 생성 로그 전송
        questHistoryService.logQuest(savedQuest);

        return convertToDto(savedQuest);
    }

    @Transactional
    public void changeQuestState(QuestStateChangeRequest request) {
        Quest quest = questRepository.findById(request.getQuestId())
                .orElseThrow(() -> new IllegalArgumentException("퀘스트를 찾을 수 없습니다."));

        // 부모퀘스트인 경우 만료 체크
        if (quest.getType() == Quest.QuestType.PARENT) {
            LocalDateTime nowKST = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            if (quest.getEndDate().isBefore(nowKST) &&
                    quest.getState() != QuestState.COMPLETED &&
                    quest.getState() != QuestState.EXPIRED) {

                log.info("⏰ 상태 변경 시 만료된 퀘스트 발견: {}", quest.getName());
                throw new IllegalArgumentException("만료된 퀘스트는 상태를 변경할 수 없습니다.");
            }
        }

        QuestState newState;
        try {
            newState = QuestState.valueOf(request.getState().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상태입니다: " + request.getState());
        }

        QuestState currentState = quest.getState();
        validateStateTransition(currentState, newState);
        quest.changeState(newState);

        //상태 변경 로그 전송
        questHistoryService.logQuest(quest);
        if (newState == QuestState.COMPLETED) {
            giveRewardToChild(quest.getChildId(), quest.getReward(), quest.getName());
        }
    }

    @Transactional
    public void giveRewardToChild(UUID childId, Integer rewardPoint, String questName) {
        try {
            User child = userRepository.findById(childId)
                    .orElseThrow(() -> new IllegalArgumentException("아이를 찾을 수 없습니다. ID: " + childId));

            Integer currentPoint = child.getPoint();
            Integer newPoint = currentPoint + rewardPoint;
            child.setPoint(newPoint);
            userRepository.save(child);

            log.info("🎉 포인트 지급 완료! 아이: {}, 퀘스트: '{}', 지급 포인트: {}, 총 포인트: {} → {}",
                    childId, questName, rewardPoint, currentPoint, newPoint);
        } catch (Exception e) {
            log.error("❌ 포인트 지급 실패! 아이: {}, 퀘스트: '{}', 보상: {}",
                    childId, questName, rewardPoint, e);
            throw new RuntimeException("포인트 지급 중 오류가 발생했습니다.", e);
        }
    }

    private void validateStateTransition(QuestState current, QuestState target) {
        boolean isValidTransition = switch (current) {
            case PENDING_ACCEPT -> target == QuestState.IN_PROGRESS;
            case IN_PROGRESS -> target == QuestState.PENDING_APPROVAL;
            case PENDING_APPROVAL -> target == QuestState.APPROVED;
            case APPROVED -> target == QuestState.COMPLETED;
            case COMPLETED, EXPIRED -> false;
        };

        if (!isValidTransition) {
            throw new IllegalArgumentException(
                    String.format("%s에서 %s로 변경할 수 없습니다.", current.name(), target.name())
            );
        }
    }

    // 🎯 깔끔한 DTO 변환 (포인트 정보 없이)
    private QuestResponse convertToDto(Quest quest) {
        return QuestResponse.builder()
                .questId(quest.getQuestId())
                .childId(quest.getChildId())
                .type(quest.getType().name().toLowerCase())
                .name(quest.getName())
                .description(quest.getDescription())
                .state(quest.getState().name())
                .endDate(quest.getEndDate())
                .created(quest.getCreated())
                .isStatic(quest.isStatic())
                .reward(quest.getReward())
                .imageUrl(quest.getImageUrl())
                .build();
    }
}