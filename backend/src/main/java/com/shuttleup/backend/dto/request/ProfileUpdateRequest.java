package com.shuttleup.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {
    @NotBlank(message = "ユーザー名を入力してください。")
    @Size(max = 100, message = "ユーザー名は100文字以内で入力してください。")
    private String name;

    @NotBlank(message = "メールアドレスを入力してください。")
    @Email(message = "正しいメールアドレスを入力してください。")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください。")
    private String email;
}
