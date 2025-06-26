package com.popoworld.backend.market.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.market.entity.Product;
import com.popoworld.backend.market.entity.PurchaseHistory;
import com.popoworld.backend.market.purchaseHistoryKafka.PurchaseHistoryKafkaProducer;
import com.popoworld.backend.market.repository.PurchaseHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseHistoryService {
    private final PurchaseHistoryMongoRepository purchaseHistoryMongoRepository;
    private final PurchaseHistoryKafkaProducer purchaseHistoryKafkaProducer;

    // 구매 로그 전송
    public void logPurchase(Product product, int amount, UUID childId) {
        try {
            PurchaseHistory history = new PurchaseHistory(
                    null, // MongoDB가 자동으로 ObjectId 생성
                    product.getUser() == null ? "npc" : "parent",
                    product.getProductName(),
                    product.getProductPrice(),
                    amount,
                    LocalDateTime.now(ZoneId.of("Asia/Seoul")),
                    childId.toString(), // 🔥 UUID -> String 변환
                    product.getProductId().toString(), // 🔥 UUID -> String 변환
                    product.getUser() != null ? product.getUser().getParent().getUserId().toString() : null, // 🔥 UUID -> String 변환
                    product.getLabel()
            );

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 🔥 날짜 배열 형태 방지
            mapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            String message = mapper.writeValueAsString(history);
            purchaseHistoryKafkaProducer.sendPurchaseHistory("purchase-history", message);

            log.info("✅ 구매 이력 Kafka 전송 완료: 상품={}, 수량={}, 자녀ID={}",
                    product.getProductName(), amount, childId);

        } catch (Exception e) {
            log.error("❌ 구매 이력 Kafka 전송 실패: 상품={}, 에러={}",
                    product.getProductName(), e.getMessage());
        }
    }
}