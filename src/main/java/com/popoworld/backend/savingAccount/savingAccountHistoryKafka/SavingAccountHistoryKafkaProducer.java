package com.popoworld.backend.savingAccount.savingAccountHistoryKafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SavingAccountHistoryKafkaProducer {
    private final KafkaTemplate<String,String>kafkaTemplate;

    public void sendSavingAccountHistory(String topic, String message){
        kafkaTemplate.send(topic,message);
    }

}
