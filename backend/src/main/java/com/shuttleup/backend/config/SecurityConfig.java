package com.shuttleup.backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class SecurityConfig {
    public static final String ISSUER = "shuttleup";

    @Bean
    SecretKey jwtKey(@Value("${SHUTTLEUP_JWT_SECRET:}") String secret) {
        // 未設定の開発環境では起動ごとに生成するため、再起動後は再ログインが必要。
        byte[] bytes;
        if (secret.isBlank()) {
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
        } else {
            bytes = Base64.getDecoder().decode(secret);
            if (bytes.length < 32) {
                throw new IllegalArgumentException("JWT署名鍵は32バイト以上をBase64で設定してください。");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtKey, UserRepository users) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtKey).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER), jwt -> {
                    try {
                        if (users.findById(Long.valueOf(jwt.getSubject()))
                                .filter(user -> user.getStatus() == UserStatus.ACTIVE
                                        && jwt.getClaim("tokenVersion") instanceof Number version
                                        && version.longValue() == user.getTokenVersion()).isPresent()) {
                            return OAuth2TokenValidatorResult.success();
                        }
                    } catch (NumberFormatException ignored) {
                        // 数値でない subject はユーザーIDとして利用できない。
                    }
                    return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token"));
                }));
        return decoder;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
                // Cookie による認証を使わず、各リクエストの Bearer トークンで認証する。
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, 401, "ログインの有効期限が切れたか、ログイン情報が無効です。")))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, 401, "ログインしてください。"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, 403, "この操作は許可されていません。")))
                .build();
    }

    private static void writeError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
