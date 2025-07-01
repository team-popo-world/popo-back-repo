package com.popoworld.backend.market.analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
//소비패턴 분석 설정
@Configuration
public class MarketAnalyticsConfig {

    @Bean
    public WebClient purchaseWebClient(){
        return WebClient.builder()
                .baseUrl("http://15.164.235.203:8001/")
                .codecs(configurer->configurer.defaultCodecs().maxInMemorySize(1024*1024))
                .build(); //한 요청당 응답 데이터를 최대 1MB까지 메모리 올릴 수 있게 설정
    }
}
