package com.popoworld.backend.User.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.repository.RefreshTokenRepository;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.*;
import com.popoworld.backend.global.token.JwtTokenProvider;
import com.popoworld.backend.global.token.RefreshToken;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void signup(SignupRequestDTO requestDto) {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일이에요.");
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setName(requestDto.getName());
        user.setSex(requestDto.getSex());
        user.setAge(requestDto.getAge());
        user.setRole(requestDto.getRole());
        user.setParentCode(requestDto.getParentCode());

        userRepository.save(user);

    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO requestDto) {
        // 비밀번호 검증 (간단히 구현한다고 가정)
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // RefreshToken 저장
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setToken(refreshToken);
        tokenEntity.setUserEmail(user.getEmail());
        tokenEntity.setExpiresAt(Instant.now().plusSeconds(60 * 60 * 24 * 7));
        refreshTokenRepository.save(tokenEntity);

        // 로그인 응답
        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                user.getRole(),
                user.getName(),
                user.getPoint()
        );
    }

    @Override
    public void logout(LogoutRequestDTO requestDto) {
        // refreshToken 삭제
        refreshTokenRepository.deleteByToken(requestDto.getRefreshToken());
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(RefreshTokenRequestDTO requestDto) {
        String requestToken = requestDto.getRefreshToken();

        // 유효한 토큰인지 확인
        if (!jwtTokenProvider.validateToken(requestToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // DB에서 토큰 존재 확인
        RefreshToken savedToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리프레시 토큰입니다."));

        // 새로운 토큰 생성
        String userEmail = savedToken.getUserEmail();
        String newAccessToken = jwtTokenProvider.generateAccessToken(userEmail);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userEmail);

        // 기존 토큰 갱신
        savedToken.setToken(newRefreshToken);
        refreshTokenRepository.save(savedToken);

        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }
}
