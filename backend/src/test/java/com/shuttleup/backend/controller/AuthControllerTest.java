package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.RegisterRequest;
import com.shuttleup.backend.dto.response.UserResponse;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.exception.EmailAlreadyExistsException;
import com.shuttleup.backend.service.AuthService;
import com.shuttleup.backend.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(authService, mock(JwtTokenService.class)))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void 登録成功時は201とユーザー情報を返しパスワードを含めない() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(UserResponse.builder()
                .id(2L).name("ユーザー").email("user@example.com").status(UserStatus.ACTIVE).build());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"ユーザー","email":"user@example.com","password":"111111"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void メールアドレスの形式が不正なら400を返す() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"ユーザー","email":"invalid-email","password":"111111"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("正しいメールアドレスを入力してください。"));

        verifyNoInteractions(authService);
    }

    @Test
    void パスワードが6文字未満なら400を返す() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"ユーザー","email":"user@example.com","password":"12345"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("パスワードは6文字以上、72文字以内で入力してください。"));

        verifyNoInteractions(authService);
    }

    @Test
    void ユーザー名が空白のみなら400を返す() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"   ","email":"user@example.com","password":"111111"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("ユーザー名を入力してください。"));

        verifyNoInteractions(authService);
    }

    @Test
    void メールアドレスが重複したら409を返す() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenThrow(new EmailAlreadyExistsException());

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"ユーザー","email":"user@example.com","password":"111111"}
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("このメールアドレスは既に登録されています。"));
    }
}
