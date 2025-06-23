package com.popoworld.backend.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SsePingScheduler {

    private final List<AbstractSseEmitters> allEmitters;

    @Scheduled(fixedRate = 15_000)
    public void sendPing() {
        allEmitters.forEach(e -> e.sendPingToAll());
        log.debug("🔄 SSE ping 전송 완료");
    }
}