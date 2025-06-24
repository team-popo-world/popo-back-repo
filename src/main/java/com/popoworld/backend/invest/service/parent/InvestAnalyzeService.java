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
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("43.203.175.63")
                        .port(8002)
                        .path("/api/invest" + path)
                        .queryParam("userId", userId)
                        .build()
                )
                .retrieve()
                .bodyToMono(Object.class);
    }
}
