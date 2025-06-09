package com.popoworld.backend.User.controller;

import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.EmailVerificationCompleteDTO;
import com.popoworld.backend.User.dto.Response.LoginResponseDTO;
import com.popoworld.backend.User.dto.Response.RefreshTokenResponseDTO;
import com.popoworld.backend.User.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 api")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새로운 유저를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequestDTO requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공!");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT 토큰을 반환합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDto) {
        LoginResponseDTO responseDto = userService.login(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 무효화하여 로그아웃합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody @Valid LogoutRequestDTO requestDto) {
        userService.logout(requestDto);
        return ResponseEntity.ok("로그아웃 완료");
    }

    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰으로 새로운 엑세스 토큰을 발급받습니다.")
    @PostMapping("/token/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(@RequestBody @Valid RefreshTokenRequestDTO requestDto) {
        RefreshTokenResponseDTO tokens = userService.refreshToken(requestDto);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "이메일 인증 메일 발송", description = "입력된 이메일로 인증 메일을 발송합니다.")
    @PostMapping("/email/verify")
    public ResponseEntity<?> sendVerificationMail(@RequestBody @Valid EmailVerificationRequestDTO requestDto) {
        userService.sendVerificationEmail(requestDto);
        return ResponseEntity.ok("인증 메일을 보냈어요!");
    }

    @Operation(summary = "이메일 인증 완료", description = "사용자가 인증 메일의 링크를 클릭해 인증을 완료합니다.")
    @PostMapping("/email/verify/complete")
    public ResponseEntity<?> completeEmailVerification(@RequestBody @Valid EmailVerificationCompleteDTO requestDto) {
        userService.completeEmailVerification(requestDto);
        return ResponseEntity.ok("이메일 인증이 완료되었어요!");
    }
}
