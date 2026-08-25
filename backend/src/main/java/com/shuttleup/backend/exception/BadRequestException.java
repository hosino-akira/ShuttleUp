package com.shuttleup.backend.exception;

/** リクエスト内容が業務ルールに反する場合の例外。 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
