package com.documentor.backend.service.auth;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import com.documentor.backend.domain.user.User;
import com.documentor.backend.infra.security.AuthenticatedUserResolver;
import com.documentor.backend.infra.security.JwtTokenProvider;
import com.documentor.backend.infra.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    public UserResult signUp(String email, String password, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 가입된 이메일입니다.");
        }

        User user = User.create(email, passwordEncoder.encode(password), nickname);
        return UserResult.from(userRepository.save(user));
    }

    public TokenResult login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        return createTokenResult(user.getId());
    }

    public TokenResult refresh(String refreshToken) {
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
        return createTokenResult(userId);
    }

    public UserResult getMe(String authorizationHeader) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        return UserResult.from(getUser(userId));
    }

    public UserResult updateMe(String authorizationHeader, String nickname) {
        Long userId = authenticatedUserResolver.resolve(authorizationHeader);
        User user = getUser(userId);
        user.updateNickname(nickname);
        return UserResult.from(userRepository.save(user));
    }

    private TokenResult createTokenResult(Long userId) {
        return new TokenResult(
                jwtTokenProvider.createAccessToken(userId),
                jwtTokenProvider.createRefreshToken(userId),
                jwtTokenProvider.getAccessTokenExpirationSeconds()
        );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
    }
}
