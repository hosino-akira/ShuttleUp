package com.shuttleup.backend.exception;

/** メールアドレスの重複を登録 API の409レスポンスへ変換する。 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException() {
        super("このメールアドレスは既に登録されています。");
    }

    public EmailAlreadyExistsException(Throwable cause) {
        super("このメールアドレスは既に登録されています。", cause);
    }
}
