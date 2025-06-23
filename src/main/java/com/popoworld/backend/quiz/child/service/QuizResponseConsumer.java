package com.popoworld.backend.quiz.child.service;

import com.popoworld.backend.invest.service.parent.ChatbotSseEmitters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizResponseConsumer {

    private final QuizSseEmitters sseEmitters;
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "quiz.response", groupId = "quiz-response-group")
    public void onResponse(@Header(KafkaHeaders.RECEIVED_KEY) String userId) {

        UUID userUUID = UUID.fromString(userId);
        String redisKey = "quiz:" + userUUID;

        String quizData = redisTemplate.opsForValue().get(redisKey);

        if (quizData != null) {
            log.info("[Quiz] 퀴즈 응답 Redis 조회 성공, SSE 전송 {}", userUUID);
            sseEmitters.send(userUUID, quizData);
            log.info("[Quiz] 퀴즈 응답 Redis 조회 성공, SSE 전송성공 {}", quizData);
        } else {
            log.warn("[Quiz] Redis에 퀴즈 결과가 없음: {}", redisKey);
        }
    }
}