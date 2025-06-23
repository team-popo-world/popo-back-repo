package com.popoworld.backend.invest.service.parent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InvestAnalyzeService {

    private final WebClient webClient;

    public Mono<Object> getGraph(String path, String userId) {
        return webClient.get()
                .uri("url" + "/api/graph" + path + "?userId=" + userId)
                .retrieve()
                .bodyToMono(Object.class);
    }
}
