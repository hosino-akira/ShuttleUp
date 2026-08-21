package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.MatchCreateRequest;
import com.shuttleup.backend.dto.request.MatchUpdateRequest;
import com.shuttleup.backend.dto.response.MatchResponse;
import com.shuttleup.backend.entity.Match;
import com.shuttleup.backend.entity.Opponent;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final OpponentRepository opponentRepository;

    public MatchService(
            MatchRepository matchRepository,
            TrainingSessionRepository trainingSessionRepository,
            OpponentRepository opponentRepository) {
        this.matchRepository = matchRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.opponentRepository = opponentRepository;
    }

    /** 指定されたトレーニングセッションに紐づく試合一覧を取得する。 */
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesByTrainingSessionId(Long sessionId) {
        findTrainingSession(sessionId);
        return matchRepository.findByTrainingSessionIdOrderByIdAsc(sessionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** IDを指定して試合を取得する。 */
    @Transactional(readOnly = true)
    public MatchResponse getMatch(Long matchId) {
        return toResponse(findMatch(matchId));
    }

    /** 指定されたトレーニングセッションに試合を追加する。 */
    @Transactional
    public MatchResponse createMatch(Long sessionId, MatchCreateRequest request) {
        TrainingSession trainingSession = findTrainingSession(sessionId);
        Opponent opponent = findOpponent(request.getOpponentId());
        validateOpponentOwner(trainingSession, opponent);

        LocalDateTime now = LocalDateTime.now();
        Match match = new Match();
        match.setTrainingSession(trainingSession);
        match.setOpponent(opponent);
        applyValues(match, request);
        match.setCreatedAt(now);
        match.setUpdatedAt(now);

        return toResponse(matchRepository.save(match));
    }

    /** 試合を更新する。所属するトレーニングセッションは変更しない。 */
    @Transactional
    public MatchResponse updateMatch(Long matchId, MatchUpdateRequest request) {
        Match match = findMatch(matchId);
        Opponent opponent = findOpponent(request.getOpponentId());
        validateOpponentOwner(match.getTrainingSession(), opponent);
        match.setOpponent(opponent);
        applyValues(match, request);
        match.setUpdatedAt(LocalDateTime.now());
        return toResponse(matchRepository.save(match));
    }

    /** IDを指定して試合を削除する。 */
    @Transactional
    public void deleteMatch(Long matchId) {
        matchRepository.delete(findMatch(matchId));
    }

    private TrainingSession findTrainingSession(Long sessionId) {
        return trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定されたトレーニングセッションが見つかりません。"));
    }

    private Match findMatch(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された試合が見つかりません。"));
    }

    private Opponent findOpponent(Long opponentId) {
        return opponentRepository.findById(opponentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "指定された対戦相手が見つかりません。"));
    }

    private void validateOpponentOwner(
            TrainingSession trainingSession,
            Opponent opponent) {
        if (!trainingSession.getUser().getId().equals(opponent.getUser().getId())) {
            throw new IllegalArgumentException(
                    "指定された対戦相手はこのユーザーに属していません。");
        }
    }

    private void applyValues(Match match, MatchCreateRequest request) {
        match.setMatchDate(request.getMatchDate());
        match.setMyScore(request.getMyScore());
        match.setOpponentScore(request.getOpponentScore());
        match.setVideoUrl(request.getVideoUrl());
        match.setNote(request.getNote());
    }

    private void applyValues(Match match, MatchUpdateRequest request) {
        match.setMatchDate(request.getMatchDate());
        match.setMyScore(request.getMyScore());
        match.setOpponentScore(request.getOpponentScore());
        match.setVideoUrl(request.getVideoUrl());
        match.setNote(request.getNote());
    }

    private MatchResponse toResponse(Match match) {
        return MatchResponse.builder()
                .id(match.getId())
                .trainingSessionId(match.getTrainingSession().getId())
                .opponentId(match.getOpponent().getId())
                .opponentName(match.getOpponent().getName())
                .matchDate(match.getMatchDate())
                .myScore(match.getMyScore())
                .opponentScore(match.getOpponentScore())
                .videoUrl(match.getVideoUrl())
                .note(match.getNote())
                .createdAt(match.getCreatedAt())
                .updatedAt(match.getUpdatedAt())
                .build();
    }
}
