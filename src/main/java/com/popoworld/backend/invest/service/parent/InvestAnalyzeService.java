package com.popoworld.backend.invest.service.parent;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestAnalyzeService {

    private final WebClient webClient;
    private final UserRepository userRepository;

    public Mono<Object> getGraph(String path, UUID parentId ,UUID childId) {

        User child = userRepository.findById(childId)
                .orElseThrow(() -> new EntityNotFoundException("해당 자식이 존재하지 않습니다."));

        if (!child.getParent().getUserId().equals(parentId)) {
            try {
                throw new AccessDeniedException("부모-자식 관계가 아닙니다.");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("43.203.175.63")
                        .port(8002)
                        .path("/api/invest" + path)
                        .queryParam("userId", childId.toString())
                        .build()
                )
                .retrieve()
                .bodyToMono(Object.class);
    }
}
