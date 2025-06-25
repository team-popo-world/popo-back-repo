package com.popoworld.backend.sse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AbstractSseEmitters {

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

    public void send(UUID userId, Object data, String eventName) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                log.info("{}", eventName);
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
                if (eventName.equals("quiz")) {
                    emitters.remove(userId);
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
                log.warn("❗ emitter 없음! userId: {}", userId);
                emitters.remove(userId);
            }
        }
    }

    public void sendPingToAll() {
        emitters.forEach((userId, emitter) -> {
            try {
                log.info("❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ ❗ {}", userId);
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(userId);
            }
        });
    }
}
