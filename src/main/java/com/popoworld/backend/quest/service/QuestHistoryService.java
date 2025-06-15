package com.popoworld.backend.quest.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.quest.entity.Quest;
import com.popoworld.backend.quest.entity.QuestHistory;
import com.popoworld.backend.quest.questHistoryKafka.QuestHistoryKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestHistoryService {
    private final QuestHistoryKafkaProducer questHistoryKafkaProducer;

    //퀘스트 로그 생성(상태 변경/ 생성때 이 메서드 사용함당)
    public void logQuest(Quest quest){
        try{
            QuestHistory history = new QuestHistory(
                    UUID.randomUUID(),
                    quest.getQuestId(),
                    quest.getChildId(),
                    quest.getType().name().toLowerCase(),
                    quest.getName(),
                    quest.getDescription(),
                    quest.getState(),
                    quest.getReward(),
                    LocalDateTime.now()
            );

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String json = mapper.writeValueAsString(history);

            questHistoryKafkaProducer.sendQuestHistory("quest-history", json);
            log.info("🎮 퀘스트 로그 전송: {} - {}", quest.getName(), quest.getState());

        } catch (Exception e) {
            log.error("❌ 퀘스트 로그 전송 실패: {}", quest.getName(), e);
        }

    }
}
