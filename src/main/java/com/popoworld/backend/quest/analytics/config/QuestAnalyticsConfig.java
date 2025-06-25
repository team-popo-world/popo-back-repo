package com.popoworld.backend.quest.analytics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class QuestAnalyticsConfig {

    @Bean // <- Spring Container에 Restemplate 객체를 등록
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    //RestTemplate란 Spring에서 제공하는 HTTP 라이브러리
    //다른 서버의 API를 호출할 때 사용하는 도구이다.
}
