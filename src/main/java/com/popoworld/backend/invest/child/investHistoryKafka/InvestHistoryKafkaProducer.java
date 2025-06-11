package com.popoworld.backend.invest.child.investHistoryKafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestHistoryKafkaProducer {
    private final KafkaTemplate<String,String> kafkaTemplate;

    public void sendInvestHistory(String topic, String message){
        kafkaTemplate.send(topic,message);
    }
}