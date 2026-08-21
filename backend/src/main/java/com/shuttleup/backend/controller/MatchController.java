package com.shuttleup.backend.controller;

import com.shuttleup.backend.dto.request.MatchCreateRequest;
import com.shuttleup.backend.dto.request.MatchUpdateRequest;
import com.shuttleup.backend.dto.response.MatchResponse;
import com.shuttleup.backend.service.MatchService;
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
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    /** 指定されたトレーニングセッションに紐づく試合一覧を取得する。 */
    @GetMapping("/training-sessions/{sessionId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(
                matchService.getMatchesByTrainingSessionId(sessionId));
    }

    /** IDを指定して試合を取得する。 */
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchResponse> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getMatch(matchId));
    }

    /** 指定されたトレーニングセッションに試合を追加する。 */
    @PostMapping("/training-sessions/{sessionId}/matches")
    public ResponseEntity<MatchResponse> createMatch(
            @PathVariable Long sessionId,
            @Valid @RequestBody MatchCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(matchService.createMatch(sessionId, request));
    }

    /** 試合を更新する。 */
    @PutMapping("/matches/{matchId}")
    public ResponseEntity<MatchResponse> updateMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchUpdateRequest request) {
        return ResponseEntity.ok(matchService.updateMatch(matchId, request));
    }

    /** IDを指定して試合を削除する。 */
    @DeleteMapping("/matches/{matchId}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long matchId) {
        matchService.deleteMatch(matchId);
        return ResponseEntity.noContent().build();
    }
}
