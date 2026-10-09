package com.shuttleup.backend.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/** サービスの単体テストでは認証済みユーザーを明示する。DBへの登録は行わない。 */
abstract class AuthenticatedServiceTest {
    @BeforeEach
    void ログイン状態を準備する() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("1", null, List.of()));
    }

    @AfterEach
    void ログイン状態を破棄する() {
        SecurityContextHolder.clearContext();
    }
}
