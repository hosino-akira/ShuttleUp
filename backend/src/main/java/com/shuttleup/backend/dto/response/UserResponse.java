package com.shuttleup.backend.dto.response;

import com.shuttleup.backend.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** フロントエンドに返すユーザー情報。パスワードのハッシュ値は含めない。 */
@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserStatus status;
}
