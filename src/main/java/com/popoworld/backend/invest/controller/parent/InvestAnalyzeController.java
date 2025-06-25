package com.popoworld.backend.invest.controller.parent;

import com.popoworld.backend.invest.service.parent.InvestAnalyzeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/invest")
@Tag(name = "Analyze", description = "투자 분석 그래프 api")
public class InvestAnalyzeController {

    private final InvestAnalyzeService investAnalyzeService;

    @Operation(summary = "그래프 요청", description = "그래프 요청 api")
    @PostMapping("/{graph}/{range}")
    public Mono<ResponseEntity<Object>> getGraph(
            @PathVariable String graph,
            @PathVariable String range,
            @RequestBody UUID userId
    ) {
        UUID parentId = getCurrentUserId();
        UUID childId = userId;

        String path = resolvePath(graph, range);
        return investAnalyzeService.getGraph(path, parentId, childId).map(ResponseEntity::ok);
    }

    private String resolvePath(String graph, String range) {
        return "/" + graph + "/" + range;
    }
}
