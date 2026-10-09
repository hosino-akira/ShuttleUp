package com.shuttleup.backend.service;

import com.shuttleup.backend.config.PasswordConfig;
import com.shuttleup.backend.dto.request.RegisterRequest;
import com.shuttleup.backend.dto.request.LoginRequest;
import com.shuttleup.backend.dto.request.ProfileUpdateRequest;
import com.shuttleup.backend.dto.request.PasswordChangeRequest;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.exception.EmailAlreadyExistsException;
import com.shuttleup.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder encoder = new PasswordConfig().passwordEncoder();

    @Test
    void パスワード変更は新しいハッシュを保存し認証バージョンを増やす() {
        User user = account();
        String oldHash = user.getPasswordHash();
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        service().changePassword(1L, passwordRequest("111111", "222222", "222222"));
        assertThat(user.getPasswordHash()).isNotEqualTo(oldHash).isNotEqualTo("222222");
        assertThat(encoder.matches("222222", user.getPasswordHash())).isTrue();
        assertThat(encoder.matches("111111", user.getPasswordHash())).isFalse();
        assertThat(user.getTokenVersion()).isEqualTo(1);
        assertThat(user.getUpdatedAt()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void 現在のパスワードが違う場合はハッシュと認証バージョンを変更しない() {
        User user = account();
        String oldHash = user.getPasswordHash();
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service().changePassword(1L, passwordRequest("wrong", "222222", "222222")))
                .isInstanceOf(BadRequestException.class).hasMessage("現在のパスワードが正しくありません。");
        assertThat(user.getPasswordHash()).isEqualTo(oldHash);
        assertThat(user.getTokenVersion()).isZero();
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 現在と同じパスワードは拒否する() {
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(account()));
        assertThatThrownBy(() -> service().changePassword(1L, passwordRequest("111111", "111111", "111111")))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("異なるもの");
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 確認が一致しない場合とUTF8上限超過はDB更新前に拒否する() {
        assertThatThrownBy(() -> service().changePassword(1L, passwordRequest("111111", "222222", "333333")))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("一致しません");
        String password = "あ".repeat(25);
        assertThatThrownBy(() -> service().changePassword(1L, passwordRequest("111111", password, password)))
                .isInstanceOf(BadRequestException.class).hasMessageContaining("72バイト");
        verifyNoInteractions(userRepository);
    }

    @Test
    void 無効アカウントはパスワードを変更できない() {
        User user = account();
        user.setStatus(UserStatus.DISABLED);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service().changePassword(1L, passwordRequest("111111", "222222", "222222")))
                .isInstanceOf(DisabledException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    private PasswordChangeRequest passwordRequest(String current, String next, String confirm) {
        PasswordChangeRequest request = new PasswordChangeRequest();
        request.setCurrentPassword(current);
        request.setNewPassword(next);
        request.setConfirmPassword(confirm);
        return request;
    }

    @Test
    void 保存済みハッシュと照合してログインしパスワードは更新しない() {
        User user = account();
        when(userRepository.findByEmailIgnoreCase("User@Example.com")).thenReturn(Optional.of(user));
        var response = service().login(loginRequest(" User@Example.com ", "111111"));
        assertThat(response.user().getId()).isEqualTo(1L);
        assertThat(response.user().getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.tokenVersion()).isEqualTo(user.getTokenVersion());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void 未登録メールと誤ったパスワードは同じエラーを返す() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(account()));
        assertThatThrownBy(() -> service().login(loginRequest("user@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("メールアドレスまたはパスワードが正しくありません。");
        assertThatThrownBy(() -> service().login(loginRequest("unknown@example.com", "111111")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("メールアドレスまたはパスワードが正しくありません。");
    }

    @Test
    void パスワード未設定の既存ユーザーはログインできない() {
        User user = account();
        user.setPasswordHash(null);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service().login(loginRequest("user@example.com", "111111")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void 無効なアカウントは正しいパスワードでもログインできない() {
        User user = account();
        user.setStatus(UserStatus.DISABLED);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> service().login(loginRequest("user@example.com", "111111")))
                .isInstanceOf(DisabledException.class);
    }

    @Test
    void プロフィール更新時はメールを正規化しパスワードと状態を維持する() {
        User user = account();
        String hash = user.getPasswordHash();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(user)).thenReturn(user);
        ProfileUpdateRequest request = profileRequest("  新しい名前  ", "New@Example.com");
        var response = service().updateProfile(1L, request);
        assertThat(response.getName()).isEqualTo("新しい名前");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPasswordHash()).isEqualTo(hash);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).existsByEmailIgnoreCaseAndIdNot("new@example.com", 1L);
    }

    @Test
    void 他のユーザーのメールアドレスには変更できない() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(account()));
        when(userRepository.existsByEmailIgnoreCaseAndIdNot("taken@example.com", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service().updateProfile(1L, profileRequest("名前", "taken@example.com")))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).saveAndFlush(any());
    }

    private User account() {
        return User.builder().id(1L).name("ユーザー").email("user@example.com")
                .passwordHash(encoder.encode("111111")).status(UserStatus.ACTIVE).build();
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private ProfileUpdateRequest profileRequest(String name, String email) {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setName(name);
        request.setEmail(email);
        return request;
    }

    @Test
    void 登録時はメールを正規化しパスワードをハッシュ化して保存する() {
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        var response = service().register(request("  テストユーザー  ", "User@Example.com", "111111"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("テストユーザー");
        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        verify(userRepository).existsByEmailIgnoreCase("user@example.com");
        assertThat(saved.getPasswordHash()).isNotEqualTo("111111");
        assertThat(encoder.matches("111111", saved.getPasswordHash())).isTrue();
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getCreatedAt()).isNotNull().isEqualTo(saved.getUpdatedAt());
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getEmail()).isEqualTo("user@example.com");
    }

    @Test
    void 登録済みのメールアドレスではユーザーを作成しない() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service().register(request("ユーザー", "user@example.com", "111111")))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    void 文字数が上限内でもUTF8で72バイトを超えるパスワードは拒否する() {
        assertThatThrownBy(() -> service().register(request("ユーザー", "user@example.com", "あ".repeat(25))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("72バイト");

        verifyNoInteractions(userRepository);
    }

    @Test
    void 同時登録によるMySQLの重複キーもメール重複として扱う() {
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(
                new DataIntegrityViolationException("duplicate", new SQLException("duplicate", "23000", 1062)));

        assertThatThrownBy(() -> service().register(request("ユーザー", "user@example.com", "111111")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void 重複以外のデータベースエラーはメール重複として扱わない() {
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "not null", new SQLException("not null", "23000", 1048));
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> service().register(request("ユーザー", "user@example.com", "111111")))
                .isSameAs(failure);
    }

    private AuthService service() {
        return new AuthService(userRepository, encoder);
    }

    private RegisterRequest request(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
