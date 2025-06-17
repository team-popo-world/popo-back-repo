package com.popoworld.backend.invest.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitters {
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter create(UUID userId) {

        SseEmitter old = emitters.remove(userId);
        if (old != null) {
            old.complete(); // 또는 old.completeWithError()로도 가능
        }

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("connected"));
        } catch (Exception e) {
            emitter.completeWithError(e);
            emitters.remove(userId);
        }

        return emitter;
    }

    public void send(UUID userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("chatbot")
                        .data(data));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(userId);
            }
        }
    }
}

