package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.RegisterRequest;
import com.shuttleup.backend.dto.request.LoginRequest;
import com.shuttleup.backend.dto.request.ProfileUpdateRequest;
import com.shuttleup.backend.dto.request.PasswordChangeRequest;
import com.shuttleup.backend.dto.response.LoginResponse;
import com.shuttleup.backend.dto.response.UserResponse;
import com.shuttleup.backend.service.AuthService;
import com.shuttleup.backend.service.JwtTokenService;
import com.shuttleup.backend.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request);
        return jwtTokenService.issue(result.user(), result.tokenVersion());
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(CurrentUser.id(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse getProfile() {
        return authService.getProfile(CurrentUser.id());
    }

    /** 更新対象とアカウント状態は、クライアントから指定させない。 */
    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return authService.updateProfile(CurrentUser.id(), request);
    }

    /** 登録成功時は、作成したユーザーの基本情報と201を返す。 */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
}
