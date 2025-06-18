package com.popoworld.backend.market.purchaseHistoryKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.market.entity.PurchaseHistory;
import com.popoworld.backend.market.repository.PurchaseHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseHistoryKafkaConsumer {
    private final PurchaseHistoryMongoRepository purchaseHistoryMongoRepository;

    @KafkaListener(topics = "purchase-history", groupId = "purchase-consumer-group")
    public void consume(String message){
        System.out.println("🔍 Purchase Kafka 메시지 수신: " + message);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            PurchaseHistory history = objectMapper.readValue(message, PurchaseHistory.class);
            System.out.println("📝 파싱된 구매 데이터: " + history.toString());

            PurchaseHistory savedHistory = purchaseHistoryMongoRepository.save(history);
            System.out.println("✅ Purchase MongoDB 저장 완료 - ID: " + savedHistory.getId());

        } catch (Exception e) {
            System.err.println("❌ Purchase 에러 발생 - 메시지: " + message);
            System.err.println("❌ Purchase 에러 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
