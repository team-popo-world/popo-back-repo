package com.popoworld.backend.market.purchaseHistoryKafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryKafkaProducer {
    private final KafkaTemplate<String,String>kafkaTemplate;

    public void sendPurchaseHistory(String topic, String message){
        kafkaTemplate.send(topic,message);
    }
}
