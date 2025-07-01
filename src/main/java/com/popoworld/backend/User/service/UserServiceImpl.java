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
import com.popoworld.backend.quiz.child.service.QuizService;
import com.popoworld.backend.webpush.entity.WebPush;
import com.popoworld.backend.webpush.repository.PushRepository;
import com.popoworld.backend.webpush.service.PushSubService;
import com.popoworld.backend.webpush.service.PushSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final QuestService questService;
    private final StringRedisTemplate redisTemplate;
    private final QuizService quizService;
    private final Duration ACCESS_TTL = Duration.ofDays(1);
    private final Duration REFRESH_TTL = Duration.ofDays(7);
    private final PushRepository repository;
    private final PushSubService pushService;

    // 공통로직
    @Override
    @Transactional
    public void signup(SignupRequestDTO requestDto) throws Exception {
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
            quizService.createDefaultQuiz(user.getUserId());

            repository.findByUserId(user.getParent().getUserId())
                    .ifPresentOrElse(
                            sub -> {
                                try {
                                    pushService.sendNotification(sub, "새로운 자녀(" + user.getName() + ")가 등록됐습니다.");
                                } catch (Exception e) {
                                    log.error("푸시 알림 전송 중 오류 발생, userId={}", user.getParent().getUserId(), e);
                                    // 예외 던지지 않고 무시하여 회원가입 흐름 유지
                                }
                            },
                            () -> log.warn("부모 유저의 푸시 구독 정보가 없어 알림을 보내지 못했습니다. userId={}", user.getParent().getUserId())
                    );

        }
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO requestDto, HttpServletRequest request) {
        // 이메일 검증
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 도메인 검증
        String origin = request.getHeader("Origin");
        if (origin == null || origin.contains("localhost")) {
            log.info("✅ Swagger 또는 로컬 환경에서 접근 - 도메인 검증 생략");
        } else {
            if ("Child".equalsIgnoreCase(user.getRole()) && !origin.contains("child-popo-world-front.vercel.app")) {
                throw new IllegalStateException("자녀는 지정된 도메인에서만 로그인할 수 있어요.");
            }
            if ("Parent".equalsIgnoreCase(user.getRole()) && !origin.contains("parent-popo-world-front.vercel.app")) {
                throw new IllegalStateException("부모는 지정된 도메인에서만 로그인할 수 있어요.");
            }
        }
        

        redisTemplate.delete(loginKey(requestDto.getEmail()));
        redisTemplate.delete(refreshKey(requestDto.getEmail()));

        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());


        // RefreshToken 저장
        redisTemplate.opsForValue().set(loginKey(requestDto.getEmail()), "true", ACCESS_TTL);
        redisTemplate.opsForValue().set(refreshKey(requestDto.getEmail()), refreshToken, REFRESH_TTL);

        // 로그인 응답
        return buildUserResponse(user, accessToken, refreshToken);

    }

    @Override
    @Transactional
    public void logout(LogoutRequestDTO requestDto) {
        String userEmail = requestDto.getUserEmail();
        redisTemplate.delete(loginKey(userEmail));
        redisTemplate.delete(refreshKey(userEmail));
    }

    @Override
    @Transactional
    public RefreshTokenResponseDTO refreshToken(String requestDto) {
        String requestToken = requestDto;

        // 유효한 토큰인지 확인
        if (!jwtTokenProvider.validateToken(requestToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 이메일로 사용자 조회
        String userEmail = jwtTokenProvider.getEmailFromToken(requestToken);

        // DB에서 토큰 존재 확인
        String savedToken = redisTemplate.opsForValue().get(refreshKey(userEmail));

        if (savedToken == null || !savedToken.equals(requestToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 일치하지 않거나 만료되었습니다.");
        }

        // 사용자 정보 보회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));

        // 새로운 토큰 생성 및 갱신
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userEmail);

        redisTemplate.opsForValue().set(refreshKey(userEmail), newRefreshToken, REFRESH_TTL);
        redisTemplate.expire(loginKey(userEmail), ACCESS_TTL);

        return new RefreshTokenResponseDTO(newAccessToken, newRefreshToken);
    }

    @Override
    public LoginResponseDTO getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return buildUserResponse(user, null, null);
    }

    private String loginKey(String email) {
        return "login:" + email;
    }

    private String refreshKey(String email) {
        return "refresh:" + email;
    }

    private LoginResponseDTO buildUserResponse(User user, String accessToken, String refreshToken) {
        if ("Parent".equalsIgnoreCase(user.getRole())) {
            List<User> children = userRepository.findAllChildrenByParentId(user.getUserId());
            return new ParentLoginResponseDTO(
                    accessToken, refreshToken, user.getRole(), user.getName(), user.getParentCode(),
                    children.stream().map(c -> ChildInfoDTO.builder().user(c).build()).toList()
            );
        } else if ("Child".equalsIgnoreCase(user.getRole())) {
            return new ChildLoginResponseDTO(
                    accessToken, refreshToken, user.getRole(), user.getName(), user.getPoint()
            );
        }
        throw new IllegalArgumentException("role 값은 'Parent' 또는 'Child'만 가능합니다.");
    }

}
