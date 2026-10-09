package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** ユーザー登録に必要な入力。ID とアカウント状態はサーバー側で決定する。 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "ユーザー名を入力してください。")
    @Size(max = 100, message = "ユーザー名は100文字以内で入力してください。")
    private String name;

    @NotBlank(message = "メールアドレスを入力してください。")
    @Email(message = "正しいメールアドレスを入力してください。")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
    private String email;

    @NotBlank(message = "パスワードを入力してください。")
    @Size(min = 6, max = 72, message = "パスワードは6文字以上、72文字以内で入力してください。")
    private String password;
}
