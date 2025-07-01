package com.popoworld.backend.User.controller;

import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.LoginResponseDTO;
import com.popoworld.backend.User.dto.Response.RefreshTokenResponseDTO;
import com.popoworld.backend.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.popoworld.backend.global.token.SecurityUtil.getCurrentUserId;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 api")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequestDTO requestDto) throws Exception {
        userService.signup(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDto,
                                                  HttpServletRequest request) {
        LoginResponseDTO responseDto = userService.login(requestDto, request);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + responseDto.getAccessToken());
        headers.set("Refresh-Token", responseDto.getRefreshToken());

        responseDto.setAccessToken(null);
        responseDto.setRefreshToken(null);

        return ResponseEntity.ok().headers(headers).body(responseDto);
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequestDTO requestDto) {
        userService.logout(requestDto);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰으로 새로운 엑세스 토큰을 발급받습니다.")
    @PostMapping("/token/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        RefreshTokenResponseDTO tokens = userService.refreshToken(refreshToken);
        // 헤더에 토큰 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokens.getAccessToken());
        headers.set("Refresh-Token", tokens.getRefreshToken());

        return ResponseEntity.noContent().headers(headers).build();
    }

    @Operation(summary = "회원 정보 조회", description = "회원 정보 조회")
    @GetMapping
    public ResponseEntity<LoginResponseDTO> getUserInfo() {
        UUID userId = getCurrentUserId();
        LoginResponseDTO dto = userService.getUserInfo(userId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "튜토리얼 완료",description = "사용자의 튜토리얼을 완료 처리합니다.")
    @PutMapping("/tutorial/complete")
    public ResponseEntity<String> completeTutorial(){
        UUID userId = getCurrentUserId();
        userService.completeTutorial(userId);
        return ResponseEntity.ok("튜토리얼이 완료되었습니다.");
    }
}