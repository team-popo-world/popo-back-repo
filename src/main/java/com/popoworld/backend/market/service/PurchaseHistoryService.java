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
            // 한국시간을 UTC로 변환해서 저장 (MongoDB에서 +09:00로 표시되도록)
            LocalDateTime koreaTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            LocalDateTime utcTime = koreaTime.minusHours(9); // 한국시간에서 9시간 빼기

            PurchaseHistory history = new PurchaseHistory(
                    null,
                    product.getUser() == null ? "npc" : "parent",
                    product.getProductName(),
                    product.getProductPrice(),
                    amount,
                    utcTime, // 👈 UTC 시간으로 저장
                    childId.toString(),
                    product.getProductId().toString(),
                    product.getUser() != null ? product.getUser().getParent().getUserId().toString() : null,
                    product.getLabel()
            );

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setTimeZone(TimeZone.getTimeZone("Asia/Seoul")); // 👈 이제 UTC → KST 변환

            String message = mapper.writeValueAsString(history);
            purchaseHistoryKafkaProducer.sendPurchaseHistory("purchase-history", message);

            log.info("✅ 구매 이력 Kafka 전송 완료: 상품={}, 수량={}, 자녀ID={}, 한국시간={}",
                    product.getProductName(), amount, childId, koreaTime);

        } catch (Exception e) {
            log.error("❌ 구매 이력 Kafka 전송 실패: 상품={}, 에러={}",
                    product.getProductName(), e.getMessage());
        }
    }
}