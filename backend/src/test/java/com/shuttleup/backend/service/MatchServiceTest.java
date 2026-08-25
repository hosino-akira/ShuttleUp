package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.MatchCreateRequest;
import com.shuttleup.backend.dto.request.MatchUpdateRequest;
import com.shuttleup.backend.entity.Match;
import com.shuttleup.backend.entity.Opponent;
import com.shuttleup.backend.entity.TrainingSession;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    @Mock MatchRepository matchRepository;
    @Mock TrainingSessionRepository trainingSessionRepository;
    @Mock OpponentRepository opponentRepository;

    @Test
    void 引き分けは新規登録できない() {
        User user = User.builder().id(1L).build();
        when(trainingSessionRepository.findById(1L)).thenReturn(Optional.of(
                TrainingSession.builder().id(1L).user(user).build()));
        when(opponentRepository.findById(2L)).thenReturn(Optional.of(
                Opponent.builder().id(2L).user(user).build()));
        MatchCreateRequest request = new MatchCreateRequest();
        request.setOpponentId(2L); request.setMatchDate(LocalDate.now());
        request.setMyScore(21); request.setOpponentScore(21);
        assertThatThrownBy(() -> service().createMatch(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("引き分けの試合結果は登録できません。");
    }

    @Test
    void 引き分けは更新できない() {
        User user = User.builder().id(1L).build();
        Match match = Match.builder().id(3L)
                .trainingSession(TrainingSession.builder().id(1L).user(user).build()).build();
        when(matchRepository.findById(3L)).thenReturn(Optional.of(match));
        when(opponentRepository.findById(2L)).thenReturn(Optional.of(
                Opponent.builder().id(2L).user(user).build()));
        MatchUpdateRequest request = new MatchUpdateRequest();
        request.setOpponentId(2L); request.setMatchDate(LocalDate.now());
        request.setMyScore(10); request.setOpponentScore(10);
        assertThatThrownBy(() -> service().updateMatch(3L, request))
                .isInstanceOf(BadRequestException.class);
    }

    private MatchService service() {
        return new MatchService(matchRepository, trainingSessionRepository, opponentRepository);
    }
}
