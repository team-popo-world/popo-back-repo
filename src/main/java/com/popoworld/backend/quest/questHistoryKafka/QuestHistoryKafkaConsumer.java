package com.popoworld.backend.quest.questHistoryKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.quest.entity.QuestHistory;
import com.popoworld.backend.quest.repository.QuestHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestHistoryKafkaConsumer {
    private final QuestHistoryMongoRepository questHistoryMongoRepository;

    //quest-history라는 토픽에서 메세지를 받음.
    //받은 메시지를 JSON형태에서 QuestHistory 객체로 변환
    //그 데이터를 몽고디비에 저장
    @KafkaListener(topics = "quest-history", groupId = "quest-consumer-group")
    public void consume(String message){
        System.out.println("🔍 Quest Kafka 메시지 수신: " + message);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            QuestHistory history = objectMapper.readValue(message, QuestHistory.class);
            System.out.println("📝 파싱된 퀘스트 데이터: " + history.toString());

            QuestHistory savedHistory = questHistoryMongoRepository.save(history);
            System.out.println("✅ Quest MongoDB 저장 완료 - ID: " + savedHistory.getId());
            System.out.println("📊 저장된 퀘스트 데이터 확인: " + savedHistory);

        } catch (Exception e) {
            System.err.println("❌ Quest 에러 발생 - 메시지: " + message);
            System.err.println("❌ Quest 에러 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
