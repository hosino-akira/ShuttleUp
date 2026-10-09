package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.RegisterRequest;
import com.shuttleup.backend.dto.request.LoginRequest;
import com.shuttleup.backend.dto.request.ProfileUpdateRequest;
import com.shuttleup.backend.dto.request.PasswordChangeRequest;
import com.shuttleup.backend.dto.response.UserResponse;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.entity.UserStatus;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.exception.EmailAlreadyExistsException;
import com.shuttleup.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** ログイン時点の認証バージョンを、プロフィールの公開情報とは分けて保持する。 */
    public record LoginResult(UserResponse user, long tokenVersion) { }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail().strip())
                .orElseThrow(this::invalidCredentials);
        // matches は保存済みハッシュ内のソルトを利用して照合する。
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        requireActive(user);
        return new LoginResult(toResponse(user), user.getTokenVersion());
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        if (request.getCurrentPassword().getBytes(StandardCharsets.UTF_8).length > 72
                || request.getNewPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("パスワードはUTF-8で72バイト以内で入力してください。");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("新しいパスワードと確認用パスワードが一致しません。");
        }
        User user = userRepository.findByIdForUpdate(userId).orElseThrow(this::invalidCredentials);
        requireActive(user);
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("現在のパスワードが正しくありません。");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("新しいパスワードは現在のパスワードと異なるものを入力してください。");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.saveAndFlush(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return toResponse(findActiveUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findActiveUser(userId);
        String email = request.getEmail().strip().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new EmailAlreadyExistsException();
        }
        user.setName(request.getName().strip());
        user.setEmail(email);
        user.setUpdatedAt(LocalDateTime.now());
        try {
            return toResponse(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateKey(exception)) {
                throw new EmailAlreadyExistsException(exception);
            }
            throw exception;
        }
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(this::invalidCredentials);
        requireActive(user);
        return user;
    }

    private void requireActive(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DisabledException("このアカウントは無効になっています。");
        }
    }

    private BadCredentialsException invalidCredentials() {
        return new BadCredentialsException("メールアドレスまたはパスワードが正しくありません。");
    }

    /** 入力を正規化し、パスワードをハッシュ化して新しいユーザーを登録する。 */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // BCrypt の上限は文字数ではなく UTF-8 のバイト数で確認する。
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BadRequestException("パスワードはUTF-8で72バイト以内で入力してください。");
        }

        String email = request.getEmail().strip().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException();
        }

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .name(request.getName().strip())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        try {
            // 同時登録の競合も、この処理内で検出できるように SQL を実行する。
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            if (isDuplicateKey(exception)) {
                throw new EmailAlreadyExistsException(exception);
            }
            throw exception;
        }

        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
    }

    private boolean isDuplicateKey(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            // MySQL の重複キーエラー。他の整合性エラーは重複として扱わない。
            if (cause instanceof SQLException sqlException && sqlException.getErrorCode() == 1062) {
                return true;
            }
        }
        return false;
    }
}
