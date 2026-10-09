package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {
    @NotBlank(message = "現在のパスワードを入力してください。")
    @Size(max = 72, message = "現在のパスワードは72文字以内で入力してください。")
    private String currentPassword;

    @NotBlank(message = "新しいパスワードを入力してください。")
    @Size(min = 6, max = 72, message = "新しいパスワードは6文字以上、72文字以内で入力してください。")
    private String newPassword;

    @NotBlank(message = "新しいパスワードをもう一度入力してください。")
    @Size(max = 72, message = "確認用パスワードは72文字以内で入力してください。")
    private String confirmPassword;
}
