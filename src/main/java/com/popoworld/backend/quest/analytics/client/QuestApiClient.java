package com.popoworld.backend.quest.analytics.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class QuestApiClient {
    private final RestTemplate restTemplate;
    private final String questApiBaseUrl = "http://43.203.175.69:8000";

    //일일퀘스트 완료율 불러오기
    public Object getDailyCompletionRate(UUID childId,String period){
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/daily/completion_rate") //base URL 뒤에 붙는 API 엔드포인트 설정
                .queryParam("childId",childId.toString())
                .queryParam("period",period)
                .toUriString(); //지금까지 조립한 URL을 최종적으로 문자열로 변환해 저장

        log.info("일일 퀘스트 완료율 API 호출: {}",url);
        return restTemplate.getForObject(url, Object.class);
        //RestTemplate를 이용해 GET 방식으로 HTTP 요청을 보냄.
        //첫번째 인자는 요청을 보낼 주소, 두번째인자는 응답 데이터를 어떤 형태로 받을지 지정

        //흐름: 입력받은 UUID와 기간으로 URL을 생성하고
        //해당 URL로 GET요청을 보내
        //결과를 Object타입으로 그대로 반환
    }

    //부모퀘스트 완료율 불러오기
    public Object getParentCompletionRate(UUID childId, String period){
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/parent/completion_rate")
                .queryParam("childId",childId.toString())
                .queryParam("period",period)
                .toUriString();

        log.info("부모 퀘스트 완료율 API 호출: {}",url);
        return restTemplate.getForObject(url,Object.class);
    }

    //일일퀘스트 완료시간 불러오기
    public Object getDailyCompletionTime(UUID childId, String period){
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/daily/completion_time")
                .queryParam("childId",childId.toString())
                .queryParam("period",period)
                .toUriString();
        log.info("일일퀘스트 완료시간 API 호출: {}",url);
        return restTemplate.getForObject(url, Object.class);
    }

    // 부모퀘스트 완료 시간 불러오기
    public Object getParentCompletionTime(UUID childId, String period) {
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/parent/completion_time")
                .queryParam("childId", childId.toString())
                .queryParam("period", period)
                .toUriString();

        log.info("부모퀘스트 완료 시간 API 호출: {}", url);
        return restTemplate.getForObject(url, Object.class);
    }

    // 부모퀘스트 보상-완료율 그래프 불러오기
    public Object getParentCompletionReward(UUID childId, String period) {
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/parent/completion_reward")
                .queryParam("childId", childId.toString())
                .queryParam("period", period)
                .toUriString();

        log.info("부모퀘스트 보상-완료율 그래프 API 호출: {}", url);
        return restTemplate.getForObject(url, Object.class);
    }

    // 일일퀘스트 승인 시간 불러오기
    public Object getDailyApprovalTime(UUID childId, String period) {
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/daily/approval_time")
                .queryParam("childId", childId.toString())
                .queryParam("period", period)
                .toUriString();

        log.info("일일퀘스트 승인 시간 API 호출: {}", url);
        return restTemplate.getForObject(url, Object.class);
    }

    // 부모퀘스트 승인 시간 불러오기
    public Object getParentApprovalTime(UUID childId, String period) {
        String url = UriComponentsBuilder.fromHttpUrl(questApiBaseUrl)
                .path("/graph/parent/approval_time")
                .queryParam("childId", childId.toString())
                .queryParam("period", period)
                .toUriString();

        log.info("부모퀘스트 승인 시간 API 호출: {}", url);
        return restTemplate.getForObject(url, Object.class);
    }
}
