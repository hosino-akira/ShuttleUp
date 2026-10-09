package com.shuttleup.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/** JWT の subject をユーザーIDとして扱い、リクエストのIDを信用しない。 */
public final class CurrentUser {
    private CurrentUser() { }

    public static Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("ログインしてください。");
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("ログイン情報が無効です。");
        }
    }

    public static void requireOwner(Long ownerId) {
        if (!Objects.equals(id(), ownerId)) {
            throw new AccessDeniedException("このデータへのアクセスは許可されていません。");
        }
    }
}
