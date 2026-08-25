package com.shuttleup.backend.controller;

import com.shuttleup.backend.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void 引き分け入力エラーはHTTP400になる() {
        var response = new ApiExceptionHandler().handleBadRequest(
                new BadRequestException("引き分けの試合結果は登録できません。"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry(
                "message", "引き分けの試合結果は登録できません。");
    }
}
