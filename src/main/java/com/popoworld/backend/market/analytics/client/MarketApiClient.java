package com.popoworld.backend.market.analytics.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketApiClient {

    private final WebClient purchaseWebClient;


    public Object getDashboardData(UUID childId, Integer days) {
        log.info("🏠 대시보드 데이터 API 호출 시작: childId={}, days={}", childId, days);
        log.info("🌐 요청 URL: http://43.203.175.69:8001/api/dashboard/{}?days={}", childId, days);

        try {
            return purchaseWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/dashboard/{child_id}")
                            .queryParam("days", days)
                            .build(childId.toString()))
                    .retrieve()
                    .onStatus(
                            status -> !status.is2xxSuccessful(),
                            clientResponse -> {
                                log.error("❌ HTTP 에러: status={}, headers={}",
                                        clientResponse.statusCode(),
                                        clientResponse.headers().asHttpHeaders());
                                return clientResponse.bodyToMono(String.class)
                                        .map(body -> {
                                            log.error("❌ 에러 응답 본문: {}", body);
                                            return new RuntimeException("API 호출 실패: " + body);
                                        });
                            }
                    )
                    .bodyToMono(Object.class)
                    .timeout(Duration.ofSeconds(30))
                    .doOnSuccess(result -> log.info("✅ 대시보드 API 호출 성공: {}", result))
                    .doOnError(error -> log.error("❌ 대시보드 API 호출 실패: {}", error.getMessage(), error))
                    .block();

        } catch (Exception e) {
            log.error("❌ API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("ML API 호출 실패", e);
        }
    }
}
