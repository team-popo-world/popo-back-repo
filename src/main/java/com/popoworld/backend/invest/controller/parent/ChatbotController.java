package com.popoworld.backend.invest.controller.parent;

import com.popoworld.backend.global.token.JwtTokenProvider;
import com.popoworld.backend.invest.dto.parent.dto.request.*;
import com.popoworld.backend.invest.dto.parent.dto.response.CustomScenarioListDTO;
import com.popoworld.backend.invest.service.parent.ChatbotSseEmitters;
import com.popoworld.backend.invest.service.parent.ParentInvestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
@Tag(name="Chatbot", description = "시나리오 업데이트 챗봇 API")
public class ChatbotController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ParentInvestService parentInvestService;
    private final ChatbotSseEmitters sseEmitters;

    @Operation(summary = "시나리오 수정 세팅", description = "시나리오 불러오기 및 redis에 임시 저장")
    @PostMapping("/edit-scenario")
    public ResponseEntity<?> set(@RequestBody ChatbotSetRequestDTO requestDTO) {
        //요청데이터 받아서
        UUID userId = getCurrentUserId();

        //redis에 시나리오 임시저장
        parentInvestService.setEditScenario(userId, requestDTO);

        //응답 반환
        return ResponseEntity.ok("시나리오 불러오기 완료");
    }

    @Operation(summary = "채팅 입력", description = "채팅으로 시나리오 업데이트 요청")
    @PostMapping("/message")
    public ResponseEntity<?> chat(@RequestBody ChatbotEditRequestDTO requestDTO) {
        //요청데이터 받아서
        UUID userId = getCurrentUserId();
        UUID requestId = UUID.randomUUID();
        //ml fastapi 호출
        parentInvestService.processChatMessage(userId, requestDTO, requestId);

        //응답 반환
        return ResponseEntity.accepted().build(); // 비동기이므로 즉시 응답
    }

    @Operation(summary = "SSE 연결", description = "챗봇 업데이트 알림용 SSE 연결")
    @GetMapping("/sse")
    public SseEmitter connect(HttpServletRequest request) throws AccessDeniedException {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new AccessDeniedException("Missing or invalid token");
        }

        UUID userId = UUID.fromString(jwtTokenProvider.getUserIdFromToken(token.substring(7)));
        return sseEmitters.create(userId);
    }


    @Operation(
            summary = "ML 커스텀 시나리오로 기존 시나리오 저장",
            description = "커스텀한 시나리오가 마음에 들면 저장"
    )
    @PostMapping("/save")
    public ResponseEntity<String> saveScenario(@RequestBody SaveCustomScenarioRequestDTO request) {
        // 저장요청 받으면
        UUID userId = getCurrentUserId();
        // 저장소에 저장
        parentInvestService.saveScenario(userId, request);
        return ResponseEntity.ok("저장 성공");
    }


    @Operation(summary = "시나리오 리스트 중 하나 삭제",
            description = "오래되거나 마음에 들지 않는 시나리오 삭제"
    )
    @PostMapping("/delete")
    public ResponseEntity<?> deleteScenario(@RequestBody DeleteCustomScenarioRequestDTO request) {
        // 데이터 삭제
        parentInvestService.deleteScenario(request.getScenarioId());
        return ResponseEntity.ok("삭제 성공");
    }



    @Operation(summary = "시나리오 리스트 조회",
            description = "시나리오 리스트 조회"
    )
    @PostMapping("/items")
    public ResponseEntity<CustomScenarioListDTO> getScenarios(@RequestBody ScenarioListDTO requestDTO) {
        UUID childId = requestDTO.getChildId();
        PageRequest pageRequest = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(), Sort.by(Sort.Direction.DESC, "createAt"));
        CustomScenarioListDTO scenarios = parentInvestService.getScenarioList(childId, requestDTO.getChapterId(), pageRequest);
        return ResponseEntity.ok(scenarios);
    }
}
