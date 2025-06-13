package com.popoworld.backend.invest.controller.parent;

import com.popoworld.backend.invest.dto.parent.dto.ChatbotRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.GetCustomScenarioListResponseDTO;
import com.popoworld.backend.invest.dto.parent.dto.SaveCustomScenarioRequestDTO;
import com.popoworld.backend.invest.dto.parent.dto.DeleteCustomScenarioRequestDTO;
import com.popoworld.backend.invest.entity.InvestScenario;
import com.popoworld.backend.invest.service.parent.ParentInvestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chatbot")
@Tag(name="Chatbot", description = "시나리오 업데이트 챗봇 API")
public class ChatbotController {

    private final ParentInvestService parentInvestService;

//    @Operation(summary = "채팅 입력", description = "채팅으로 시나리오 업데이트 요청")
//    @PostMapping("/message")
//    public ResponseEntity<?> chat(@RequestBody ChatbotRequestDTO) {
//        //요청데이터 받아서
//
//        //ml fastapi 호출
//
//        //응답 반환
//
//    }
//
//
//    @Operation(
//            summary = "ML 커스텀 시나리오로 기존 시나리오 저장",
//            description = "커스텀한 시나리오가 마음에 들면 저장"
//    )
//    @PostMapping("/save")
//    public ResponseEntity<String> saveScenario(@RequestBody SaveCustomScenarioRequestDTO request) {
//        // 저장요청 받으면
//
//        // 저장소에 저장
//
//    }


    @Operation(summary = "시나리오 리스트 중 하나 삭제",
            description = "오래되거나 마음에 들지 않는 시나리오 삭제"
    )
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteScenario(@RequestBody DeleteCustomScenarioRequestDTO request) {
        // 데이터 삭제
        parentInvestService.deleteScenario(request.getScenarioId());
        return ResponseEntity.ok("삭제 성공");
    }



    @Operation(summary = "시나리오 리스트 조회",
            description = "시나리오 리스트 조회"
    )
    @GetMapping("/items")
    public ResponseEntity<List<GetCustomScenarioListResponseDTO>> getScenarios(@RequestParam int page, @RequestParam int size) {
        UUID childId = getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));
        List<GetCustomScenarioListResponseDTO> scenarios = parentInvestService.getScenarioList(childId, pageRequest);
        return ResponseEntity.ok(scenarios);
    }
}
