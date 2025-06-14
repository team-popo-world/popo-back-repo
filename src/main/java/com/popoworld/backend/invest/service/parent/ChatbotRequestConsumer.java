package com.popoworld.backend.invest.service.parent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.popoworld.backend.invest.dto.parent.dto.kafka.ChatKafkaPayload;
import com.popoworld.backend.invest.dto.parent.dto.request.ChatbotStoryRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatbotRequestConsumer {

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chatbot.request", groupId = "chatbot-worker-group")
    public void onRequest(@Payload String messageJson, @Header(KafkaHeaders.RECEIVED_KEY) UUID userId) throws JsonProcessingException {
        System.out.println("messageJson" + messageJson);
        // 1. JSON → 객체
        ChatKafkaPayload payload = objectMapper.readValue(messageJson, ChatKafkaPayload.class);
        System.out.println("payload" + payload);
        ChatbotStoryRequestDTO apiRequest = ChatbotStoryRequestDTO.builder()
                .chapterId(payload.getChapterId())
                .story("[\n  {\n    \"turn_number\": 1,\n    \"result\": \"\",\n    \"news\": \"내일은 마을에서 큰 축제가 열릴 거예요!\",\n    \"news_hint\": \"축제에 잘 어울리는 집이 인기를 끌 수 있어요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 100,\n        \"current_value\": 100,\n        \"expectation\": \"축제에서 빠르게 지은 집이 주목을 받을 수 있지만, 안전성이 떨어질 수 있어요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 100,\n        \"current_value\": 100,\n        \"expectation\": \"적당히 튼튼한 집이 축제에서 인기를 끌 수 있지만, 완벽하지는 않을 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 100,\n        \"current_value\": 100,\n        \"expectation\": \"튼튼한 집이 안전하지만, 축제에서 주목받기에는 시간이 걸릴 수 있어요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 2,\n    \"result\": \"축제가 열렸어요. 첫째 돼지의 볏짚집은 너무 약해서 사람들이 별로 좋아하지 않았어요. 둘째 돼지의 나무집은 예쁘고 튼튼하다는 칭찬을 받았어요. 셋째 돼지의 벽돌집은 아직 완성되지 않아서 축제에 참여하지 못했어요.\",\n    \"news\": \"다음에는 큰 바람이 불어서 집들이 흔들릴 수 있어요.\",\n    \"news_hint\": \"튼튼한 집이 바람에 더 잘 견딜 수 있을 것 같아요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 100,\n        \"current_value\": 90,\n        \"expectation\": \"바람이 불면 약한 집이 쉽게 무너질 수 있어요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 100,\n        \"current_value\": 110,\n        \"expectation\": \"바람이 불어도 적당히 튼튼한 집은 괜찮을 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 100,\n        \"current_value\": 95,\n        \"expectation\": \"바람이 불면 완성되지 않은 집은 더 위험해요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 3,\n    \"result\": \"큰 바람이 불었어요. 첫째 돼지의 볏짚집은 완전히 무너졌어요. 둘째 돼지의 나무집은 잘 견뎌냈고, 셋째 돼지의 벽돌집은 조금 흔들렸지만 큰 피해는 없었어요.\",\n    \"news\": \"다음에는 마을에서 돼지들이 집을 지을 수 있는 대회가 열려요.\",\n    \"news_hint\": \"대회에서 튼튼한 집이 더 높은 점수를 받을 수 있을 것 같아요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 90,\n        \"current_value\": 70,\n        \"expectation\": \"대회에서 약한 집은 점수를 받을 수 없어요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 110,\n        \"current_value\": 120,\n        \"expectation\": \"대회에서 적당히 튼튼한 집이 높은 점수를 받을 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 95,\n        \"current_value\": 105,\n        \"expectation\": \"대회에서 튼튼한 집이 점수를 잘 받을 수 있지만, 시간이 걸릴 수 있어요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 4,\n    \"result\": \"대회가 열렸어요. 둘째 돼지의 나무집은 예쁘고 튼튼해서 높은 점수를 받았어요. 셋째 돼지의 벽돌집은 튼튼하지만 시간이 걸려서 점수가 낮았어요. 첫째 돼지는 집이 무너져서 대회에 참가하지 못했어요.\",\n    \"news\": \"다음에는 비가 많이 올 거예요.\",\n    \"news_hint\": \"비가 많이 오면 약한 집이 더 위험할 수 있어요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 70,\n        \"current_value\": 60,\n        \"expectation\": \"비가 오면 약한 집이 쉽게 젖어서 무너질 수 있어요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 120,\n        \"current_value\": 130,\n        \"expectation\": \"비가 오면 튼튼한 집은 잘 견딜 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 105,\n        \"current_value\": 100,\n        \"expectation\": \"비가 오면 튼튼한 집이 안전하지만, 완성되지 않은 부분은 문제가 될 수 있어요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 5,\n    \"result\": \"비가 많이 왔어요. 첫째 돼지의 볏짚집은 젖어서 무너졌어요. 둘째 돼지의 나무집은 잘 견뎌냈고, 셋째 돼지의 벽돌집은 안전했지만 아직 완성되지 않아서 점수가 낮았어요.\",\n    \"news\": \"다음에는 마을에서 돼지들이 집을 꾸미는 대회가 열려요.\",\n    \"news_hint\": \"예쁘고 튼튼한 집이 인기를 끌 수 있을 것 같아요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 60,\n        \"current_value\": 50,\n        \"expectation\": \"꾸미기 대회에서 약한 집은 인기를 끌기 어려워요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 130,\n        \"current_value\": 140,\n        \"expectation\": \"꾸미기 대회에서 예쁘고 튼튼한 집이 높은 점수를 받을 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 100,\n        \"current_value\": 95,\n        \"expectation\": \"꾸미기 대회에서 튼튼한 집이 좋지만, 시간이 걸려서 점수가 낮을 수 있어요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 6,\n    \"result\": \"꾸미기 대회가 열렸어요. 둘째 돼지의 나무집은 예쁘고 튼튼해서 높은 점수를 받았어요. 셋째 돼지의 벽돌집은 꾸미기에는 시간이 걸려서 점수가 낮았어요. 첫째 돼지는 집이 무너져서 대회에 참가하지 못했어요.\",\n    \"news\": \"다음에는 마을에 큰 불이 날 수 있어요.\",\n    \"news_hint\": \"튼튼한 집이 불에 잘 견딜 수 있을 것 같아요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 50,\n        \"current_value\": 40,\n        \"expectation\": \"불이 나면 약한 집은 쉽게 타버릴 수 있어요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 140,\n        \"current_value\": 150,\n        \"expectation\": \"불이 나면 튼튼한 집이 잘 견딜 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 95,\n        \"current_value\": 90,\n        \"expectation\": \"불이 나면 튼튼한 집이 안전하지만, 완성되지 않은 부분은 문제가 될 수 있어요.\"\n      }\n    ]\n  },\n  {\n    \"turn_number\": 7,\n    \"result\": \"큰 불이 났어요. 첫째 돼지의 볏짚집은 타버렸어요. 둘째 돼지의 나무집은 잘 견뎌냈고, 셋째 돼지의 벽돌집은 안전했지만 아직 완성되지 않아서 점수가 낮았어요.\",\n    \"news\": \"이제 마을에서 돼지들이 집을 지을 수 있는 새로운 기회가 생길 거예요.\",\n    \"news_hint\": \"튼튼한 집이 다음 기회에서 더 잘할 수 있을 것 같아요.\",\n    \"stocks\": [\n      {\n        \"name\": \"첫째 돼지\",\n        \"risk_level\": \"고위험 고수익\",\n        \"description\": \"무엇이든 빠르게 해요. 조금 위험해도 다시 고치면 된다고 생각해요.\",\n        \"before_value\": 40,\n        \"current_value\": 30,\n        \"expectation\": \"새로운 기회에서 약한 집은 인기를 끌기 어려워요.\"\n      },\n      {\n        \"name\": \"둘째 돼지\",\n        \"risk_level\": \"균형형\",\n        \"description\": \"조금 빠르게, 조금 튼튼하게! 둘 다 하고 싶은 균형형 돼지예요.\",\n        \"before_value\": 150,\n        \"current_value\": 160,\n        \"expectation\": \"새로운 기회에서 튼튼한 집이 인기를 끌 수 있어요.\"\n      },\n      {\n        \"name\": \"셋째 돼지\",\n        \"risk_level\": \"장기 안정형\",\n        \"description\": \"천천히 하지만 튼튼하게 지어요. 안전이 가장 중요하다고 생각해요.\",\n        \"before_value\": 90,\n        \"current_value\": 85,\n        \"expectation\": \"새로운 기회에서 튼튼한 집이 좋지만, 완성되지 않은 부분은 문제가 될 수 있어요.\"\n      }\n    ]\n  }\n]")
                .editRequest(payload.getEditRequest())
                .build();

        // 2. FastAPI 호출
        String response = webClient.post()
                .uri("http://15.164.94.158:8000/edit-scenario")
                .bodyValue(apiRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("response" + response);
        // 3. Redis에 임시 저장 (30분 유지)
        String redisKey = "scenario:temp:" + userId;
        redisTemplate.opsForValue().set(redisKey, response, Duration.ofMinutes(30));

        // 4. Kafka 응답 토픽에 메세지 보냄
        kafkaTemplate.send("chatbot.response", userId.toString(), "updated");
    }
}
