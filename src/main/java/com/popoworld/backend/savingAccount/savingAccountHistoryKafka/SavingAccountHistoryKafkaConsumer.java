package com.popoworld.backend.savingAccount.savingAccountHistoryKafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.popoworld.backend.savingAccount.entity.SavingAccountHistory;
import com.popoworld.backend.savingAccount.repository.SavingAccountHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingAccountHistoryKafkaConsumer {
    private final SavingAccountHistoryMongoRepository savingAccountHistoryMongoRepository;

    @KafkaListener(topics = "saving-account-history",groupId = "saving-account-consumer-group")
    public void consume(String message){
        System.out.println("SavingAccount Kafka 메시지 수신: "+message);

        try{
            ObjectMapper objectMapper=new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            SavingAccountHistory history = objectMapper.readValue(message, SavingAccountHistory.class);
            System.out.println("📝 파싱된 저축통장 데이터: " + history.toString());

            SavingAccountHistory savedHistory = savingAccountHistoryMongoRepository.save(history);
            System.out.println("✅ SavingAccount MongoDB 저장 완료 - ID: " + savedHistory.getId());
            System.out.println("📊 저장된 저축통장 데이터 확인: " + savedHistory);
        } catch (Exception e) {
            System.err.println("❌ SavingAccount 에러 발생 - 메시지: " + message);
            System.err.println("❌ SavingAccount 에러 내용: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
