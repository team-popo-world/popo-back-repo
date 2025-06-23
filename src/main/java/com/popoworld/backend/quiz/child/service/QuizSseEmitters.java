package com.popoworld.backend.quiz.child.service;

import com.popoworld.backend.sse.AbstractSseEmitters;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Component
public class QuizSseEmitters extends AbstractSseEmitters {

    public SseEmitter create(UUID userId) {
        return super.create(userId);
    }

    public void send(UUID userId, Object data) { super.send(userId, data, "quiz"); }
}