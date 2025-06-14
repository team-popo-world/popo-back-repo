package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotEditRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotSetRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotStoryRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.response.GetCustomScenarioListResponseDTO;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.repository.InvestScenarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentInvestServiceImpl implements ParentInvestService{

    private final InvestScenarioRepository investScenarioRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;


    public void setEditScenario(UUID userId, ChatbotSetRequestDTO requestDTO){
        // 시나리오 찾아와서
        InvestScenario scenario = investScenarioRepository.findById(requestDTO.getScenarioId())
                .orElseThrow(() -> new IllegalArgumentException("시나리오가 존재하지 않습니다."));
        // 저장할 객체 만들고
        ChatbotStoryRequestDTO payload = ChatbotStoryRequestDTO.builder()
                .chapterId(scenario.getInvestChapter().getChapterId())
                .story(scenario.getStory())
                .build();


        try {
            // 형태 바꿔서 redis에 임시 저장 -> 나중에 수정 채팅 보내면 이거 꺼내서 fastapi에 요청
           String response = objectMapper.writeValueAsString(payload);
           String redisKey = "scenario:temp:" + userId;
           redisTemplate.opsForValue().set(redisKey, response, Duration.ofMinutes(30));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    };


    public void processChatMessage(UUID userId, ChatbotEditRequestDTO requestDTO){
        // 키값으로 꺼내와서
        String redisKey = "scenario:temp:" + userId;
        String story = redisTemplate.opsForValue().get(redisKey);

        try {
            ChatbotStoryRequestDTO dto = objectMapper.readValue(story, ChatbotStoryRequestDTO.class);

            ChatKafkaPayload payload = new ChatKafkaPayload();
            payload.setUserId(userId);                           // ✅ payload에 포함
            payload.setChapterId(dto.getChapterId());
            payload.setStory(dto.getStory());
            payload.setEditRequest(requestDTO.getEditRequest());

            String jsonPayload = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send("chatbot.request", userId.toString(), jsonPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka 메시지 직렬화 실패", e);
        }
    };

    public void deleteScenario(UUID scenarioId) {
        // 존재 여부 확인 (예외 처리 가능)
        InvestScenario scenario = investScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("해당 시나리오가 존재하지 않습니다."));

        // 삭제
        investScenarioRepository.delete(scenario);
    }

    public List<GetCustomScenarioListResponseDTO> getScenarioList(UUID childId, PageRequest pageRequest) {
        // 시나리오 리스트 가져온 후에
        List<InvestScenario> scenario =  investScenarioRepository.findByChildId(childId, pageRequest).getContent();
        // dto로 매핑
        return scenario.stream().map(s -> GetCustomScenarioListResponseDTO.builder().scenario(s).build()).toList();
    }
}
