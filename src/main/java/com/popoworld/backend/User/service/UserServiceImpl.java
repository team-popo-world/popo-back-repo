package com.popoworld.backend.User.service;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.dto.ChildInfoDTO;
import com.popoworld.backend.User.repository.RefreshTokenRepository;
import com.popoworld.backend.User.repository.UserRepository;
import com.popoworld.backend.User.dto.Request.*;
import com.popoworld.backend.User.dto.Response.*;
import com.popoworld.backend.global.token.JwtTokenProvider;
import com.popoworld.backend.global.token.RefreshToken;
import com.popoworld.backend.quest.repository.QuestRepository;
import com.popoworld.backend.quest.service.QuestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final QuestService questService;
    private final StringRedisTemplate redisTemplate;
    private final Duration TTL = Duration.ofDays(7); // 7일 TTL

    // 공통로직
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


        // 역할에 따라 분기
        if ("Parent".equalsIgnoreCase(requestDto.getRole())) {
            // 부모일 경우 parentCode 자동 생성
            // 코드 중복 검사
            String generatedCode;
            do {
                generatedCode = UUID.randomUUID().toString().substring(0, 8);
            } while (userRepository.existsByParentCode(generatedCode));
            user.setParentCode(generatedCode);
            user.setParent(null);
        } else if ("Child".equalsIgnoreCase(requestDto.getRole())) {
            // 자식일 경우 parentCode로 부모 찾기
            User parent = userRepository.findByParentCodeAndRole(requestDto.getParentCode(), "Parent")
                    .orElseThrow(() -> new IllegalArgumentException("유효한 부모 코드가 아니에요."));
            user.setParentCode(requestDto.getParentCode()); // 입력값 저장
            user.setParent(parent); // FK 설정
            user.setPoint(10000); // 초기 포인트 설정
        } else {
            throw new IllegalArgumentException("role 값은 'Parent' 또는 'Child'만 가능합니다.");
        }

        userRepository.save(user);

        if ("Child".equalsIgnoreCase(user.getRole())) {
            questService.createDailyQuestsForNewChild(user.getUserId());
        }
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDto) {
        // 비밀번호 검증
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        // RefreshToken 저장
        redisTemplate.opsForValue().set(user.getEmail(), refreshToken, TTL);

        // 로그인 응답
        if ("Parent".equalsIgnoreCase(user.getRole())) {
            List<User> children = userRepository.findAllChildrenByParentId(user.getUserId());
            return new ParentLoginResponseDTO(
                    accessToken,
                    refreshToken,
                    user.getRole(),
                    user.getName(),
                    user.getParentCode(),
                    children.stream()
                            .map(c -> ChildInfoDTO.builder().user(c).build())
                            .toList()
            );
        } else if ("Child".equalsIgnoreCase(user.getRole())) {
            return new ChildLoginResponseDTO(
                    accessToken,
                    refreshToken,
                    user.getRole(),
                    user.getName(),
                    user.getPoint()
            );
        } else {
            throw new IllegalArgumentException("role 값은 'Parent' 또는 'Child'만 가능합니다.");
        }
    }

    @Override
    public void logout(LogoutRequestDTO requestDto) {
        // refreshToken 삭제
        redisTemplate.delete(requestDto.getUserEmail());
    }

    @Override
    public RefreshTokenResponseDTO refreshToken(String requestDto) {
        String requestToken = requestDto;

        // 유효한 토큰인지 확인
        if (!jwtTokenProvider.validateToken(requestToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 이메일로 사용자 조회
        String userEmail = jwtTokenProvider.getEmailFromToken(requestToken);

        // DB에서 토큰 존재 확인
        String savedToken = redisTemplate.opsForValue().get(userEmail);

        if (savedToken == null || !savedToken.equals(requestToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않거나 만료되었습니다.");
        }

        // 사용자 정보 보회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        // 새로운 토큰 생성 및 갱신
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userEmail);
        redisTemplate.opsForValue().set(userEmail, newRefreshToken, TTL);

        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    @Override
    public LoginResponseDTO getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if ("Parent".equalsIgnoreCase(user.getRole())) {
            List<User> children = userRepository.findAllChildrenByParentId(user.getUserId());
            return new ParentLoginResponseDTO(
                    null,
                    null,
                    user.getRole(),
                    user.getName(),
                    user.getParentCode(),
                    children.stream()
                            .map(c -> ChildInfoDTO.builder().user(c).build())
                            .toList()
            );
        } else if ("Child".equalsIgnoreCase(user.getRole())) {
            return new ChildLoginResponseDTO(
                    null,
                    null,
                    user.getRole(),
                    user.getName(),
                    user.getPoint()
            );
        } else {
            throw new IllegalArgumentException("role 값은 'Parent' 또는 'Child'만 가능합니다.");
        }
    }



}
