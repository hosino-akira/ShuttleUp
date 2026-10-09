package com.shuttleup.backend.config;

import com.shuttleup.backend.controller.ApiExceptionHandler;
import com.shuttleup.backend.controller.AuthController;
import com.shuttleup.backend.dto.response.UserResponse;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.repository.UserRepository;
import com.shuttleup.backend.security.CurrentUser;
import com.shuttleup.backend.service.AuthService;
import com.shuttleup.backend.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 実際のフィルターチェーンと署名を使う。ユーザーはメモリ上のモックのみ。 */
@SpringJUnitConfig(SecurityIntegrationTest.TestConfig.class)
@WebAppConfiguration
class SecurityIntegrationTest {
    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    @Import({SecurityConfig.class, PasswordConfig.class, AuthService.class, JwtTokenService.class,
            AuthController.class, ApiExceptionHandler.class, OwnedController.class})
    static class TestConfig {
        @Bean UserRepository userRepository() { return mock(UserRepository.class); }
    }

    @RestController
    static class OwnedController {
        @GetMapping("/api/test/users/{id}")
        Map<String, Long> ownData(@PathVariable Long id) {
            CurrentUser.requireOwner(id);
            return Map.of("userId", id);
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired FilterChainProxy security;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtTokenService tokens;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    MockMvc mvc;
    User user;

    @BeforeEach
    void 準備() {
        reset(users);
        user = User.builder().id(1L).name("既存ユーザー").email("user@example.com")
                .passwordHash(passwords.encode("111111")).status(UserStatus.ACTIVE).build();
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(security).build();
    }

    private String token() {
        return tokens.issue(UserResponse.builder().id(1L).name(user.getName())
                .email(user.getEmail()).status(user.getStatus()).build(), user.getTokenVersion()).accessToken();
    }

    @Test
    void ログインで発行したJWTで本人のプロフィールを取得できる() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"111111\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String token = new tools.jackson.databind.ObjectMapper().readTree(body).get("accessToken").asText();
        assertThat(decoder.decode(token).getSubject()).isEqualTo("1");
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void パスワードが違う場合はJWTを発行しない() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    void 未認証と改ざん済みJWTは拒否する() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        String valid = token();
        String[] parts = valid.split("\\.");
        String invalid = parts[0] + "." + parts[1] + "." + (parts[2].charAt(0) == 'A' ? "B" : "A") + parts[2].substring(1);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + invalid))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 期限切れJWTは拒否する() throws Exception {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(SecurityConfig.ISSUER).subject("1")
                .issuedAt(Instant.now().minusSeconds(7200)).expiresAt(Instant.now().minusSeconds(3600)).build();
        String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 発行後に無効化されたユーザーは拒否する() throws Exception {
        String token = token();
        user.setStatus(UserStatus.DISABLED);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 他のユーザーIDを指定してもアクセスできない() throws Exception {
        String token = token();
        mvc.perform(get("/api/test/users/1").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        mvc.perform(get("/api/test/users/2").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
    }

    @Test
    void プロフィール更新先はJWTのユーザーIDから決まる() throws Exception {
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        mvc.perform(put("/api/auth/me").header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"name\":\"変更後\",\"email\":\"new@example.com\",\"status\":\"DISABLED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("変更後")).andExpect(jsonPath("$.status").value("ACTIVE"));
        verify(users, never()).findById(2L);
        assertThat(passwords.matches("111111", user.getPasswordHash())).isTrue();
    }

    @Test
    void 登録は未ログインでも利用できる() throws Exception {
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User created = invocation.getArgument(0);
            created.setId(2L);
            return created;
        });
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"登録者\",\"email\":\"new@example.com\",\"password\":\"111111\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void パスワード変更で古いJWTは失効し新しいパスワードでログインできる() throws Exception {
        String oldToken = token();
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        mvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + oldToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2,\"currentPassword\":\"111111\",\"newPassword\":\"222222\",\"confirmPassword\":\"222222\"}"))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(users, never()).findByIdForUpdate(2L);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token()))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"111111\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\",\"password\":\"222222\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.tokenVersion").doesNotExist());
    }

    @Test
    void 現在のパスワードが違う場合は400を返しログインを維持する() throws Exception {
        String token = token();
        when(users.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        mvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"222222\",\"confirmPassword\":\"222222\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("現在のパスワードが正しくありません。"));
        verify(users, never()).saveAndFlush(any());
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    void 未認証と短い新パスワードは更新できない() throws Exception {
        String request = "{\"currentPassword\":\"111111\",\"newPassword\":\"12345\",\"confirmPassword\":\"12345\"}";
        mvc.perform(put("/api/auth/me/password").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/auth/me/password").header("Authorization", "Bearer " + token())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
        verify(users, never()).findByIdForUpdate(any());
    }

    @Test
    void JWTに認証バージョンがなければ拒否する() throws Exception {
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(SecurityConfig.ISSUER).subject("1")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(3600)).build();
        String legacy = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + legacy)).andExpect(status().isUnauthorized());
    }
}
