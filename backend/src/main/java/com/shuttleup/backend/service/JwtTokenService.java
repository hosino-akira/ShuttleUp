package com.shuttleup.backend.service;

import com.shuttleup.backend.config.SecurityConfig;
import com.shuttleup.backend.dto.response.LoginResponse;
import com.shuttleup.backend.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtEncoder encoder;
    public static final long EXPIRES_IN_SECONDS = 3600;

    /** 秘密情報を含めず、ユーザーID・有効期限・認証バージョンを署名する。 */
    public LoginResponse issue(UserResponse user, long tokenVersion) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(SecurityConfig.ISSUER).subject(user.getId().toString())
                .claim("tokenVersion", tokenVersion)
                .issuedAt(now).expiresAt(now.plusSeconds(EXPIRES_IN_SECONDS)).build();
        String token = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", EXPIRES_IN_SECONDS, user);
    }
}
