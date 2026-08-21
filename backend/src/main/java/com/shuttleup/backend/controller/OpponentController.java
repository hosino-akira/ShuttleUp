package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.OpponentCreateRequest;
import com.shuttleup.backend.dto.request.OpponentUpdateRequest;
import com.shuttleup.backend.dto.response.OpponentResponse;
import com.shuttleup.backend.service.OpponentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class OpponentController {

    private final OpponentService opponentService;

    public OpponentController(OpponentService opponentService) {
        this.opponentService = opponentService;
    }

    /** 指定されたユーザーが登録した対戦相手一覧を取得する。 */
    @GetMapping("/users/{userId}/opponents")
    public ResponseEntity<List<OpponentResponse>> getOpponents(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                opponentService.getOpponentsByUserId(userId));
    }

    /** IDを指定して対戦相手を取得する。 */
    @GetMapping("/opponents/{opponentId}")
    public ResponseEntity<OpponentResponse> getOpponent(
            @PathVariable Long opponentId) {
        return ResponseEntity.ok(opponentService.getOpponent(opponentId));
    }

    /** 指定されたユーザーに対戦相手を登録する。 */
    @PostMapping("/users/{userId}/opponents")
    public ResponseEntity<OpponentResponse> createOpponent(
            @PathVariable Long userId,
            @Valid @RequestBody OpponentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opponentService.createOpponent(userId, request));
    }

    /** 対戦相手の名前とメモを更新する。 */
    @PutMapping("/opponents/{opponentId}")
    public ResponseEntity<OpponentResponse> updateOpponent(
            @PathVariable Long opponentId,
            @Valid @RequestBody OpponentUpdateRequest request) {
        return ResponseEntity.ok(
                opponentService.updateOpponent(opponentId, request));
    }

    /** 試合で未使用の対戦相手を削除する。 */
    @DeleteMapping("/opponents/{opponentId}")
    public ResponseEntity<Void> deleteOpponent(@PathVariable Long opponentId) {
        opponentService.deleteOpponent(opponentId);
        return ResponseEntity.noContent().build();
    }
}
