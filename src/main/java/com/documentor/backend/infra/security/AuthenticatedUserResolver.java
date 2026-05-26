package com.documentor.backend.infra.security;

import com.documentor.backend.domain.common.BusinessException;
import com.documentor.backend.domain.common.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticatedUserResolver(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Long resolve(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return jwtTokenProvider.extractUserId(authorizationHeader.substring(BEARER_PREFIX.length()));
    }
}
