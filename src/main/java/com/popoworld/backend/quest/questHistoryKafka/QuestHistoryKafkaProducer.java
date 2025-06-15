package com.popoworld.backend.quest.questHistoryKafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestHistoryKafkaProducer {
    private final KafkaTemplate<String,String>kafkaTemplate;
    public void sendQuestHistory(String topic, String message){
        kafkaTemplate.send(topic,message);
    }
    //topic이라는 이름의 통로로 message를 보낸다.

}
