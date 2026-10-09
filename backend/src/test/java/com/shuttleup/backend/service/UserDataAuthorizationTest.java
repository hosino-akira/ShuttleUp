package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.TrainingSessionCreateRequest;
import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserDataAuthorizationTest extends AuthenticatedServiceTest {
    @Test
    void リクエストのユーザーIDを書き換えても一覧取得と新規作成はできない() {
        TrainingSessionRepository sessions = mock(TrainingSessionRepository.class);
        UserRepository users = mock(UserRepository.class);
        TrainingSessionService service = new TrainingSessionService(sessions, users);
        TrainingSessionCreateRequest request = new TrainingSessionCreateRequest();
        request.setUserId(2L);
        assertDenied(() -> service.getTrainingSessions(2L));
        assertDenied(() -> service.createTrainingSession(request));
        DashboardService dashboard = new DashboardService(users, sessions, mock(MatchRepository.class),
                mock(TrainingRecordRepository.class), mock(ExerciseRepository.class), mock(OpponentRepository.class));
        assertDenied(() -> dashboard.getDashboard(2L, null, null, null, null));
        AnalysisDataService analysis = new AnalysisDataService(users, sessions,
                mock(TrainingRecordRepository.class), mock(MatchRepository.class));
        assertDenied(() -> analysis.getAnalysisData(2L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)));
        verifyNoInteractions(sessions, users);
    }

    @Test
    void 他ユーザーのリソースIDを書き換えても取得と削除はできない() {
        User other = User.builder().id(2L).build();
        TrainingSession session = TrainingSession.builder().id(10L).user(other).build();
        TrainingSessionRepository sessions = mock(TrainingSessionRepository.class);
        when(sessions.findById(10L)).thenReturn(Optional.of(session));
        TrainingSessionService sessionService = new TrainingSessionService(sessions, mock(UserRepository.class));
        assertDenied(() -> sessionService.getTrainingSessionById(10L));
        assertDenied(() -> sessionService.deleteTrainingSession(10L));
        verify(sessions, never()).delete(any());

        TrainingRecordRepository records = mock(TrainingRecordRepository.class);
        when(records.findById(20L)).thenReturn(Optional.of(
                TrainingRecord.builder().id(20L).trainingSession(session).build()));
        TrainingRecordService recordService = new TrainingRecordService(records, sessions, mock(ExerciseRepository.class));
        assertDenied(() -> recordService.getTrainingRecords(10L));
        assertDenied(() -> recordService.getTrainingRecord(20L));
        assertDenied(() -> recordService.deleteTrainingRecord(20L));
        verify(records, never()).delete(any());

        MatchRepository matches = mock(MatchRepository.class);
        when(matches.findById(30L)).thenReturn(Optional.of(Match.builder().id(30L).trainingSession(session).build()));
        MatchService matchService = new MatchService(matches, sessions, mock(OpponentRepository.class));
        assertDenied(() -> matchService.getMatch(30L));
        assertDenied(() -> matchService.deleteMatch(30L));
        verify(matches, never()).delete(any());

        OpponentRepository opponents = mock(OpponentRepository.class);
        when(opponents.findById(40L)).thenReturn(Optional.of(Opponent.builder().id(40L).user(other).build()));
        OpponentService opponentService = new OpponentService(opponents, mock(UserRepository.class), matches);
        assertDenied(() -> opponentService.getOpponent(40L));
        assertDenied(() -> opponentService.deleteOpponent(40L));
        verify(opponents, never()).delete(any());
    }

    @Test
    void 他ユーザーの自作種目と共通種目は削除できない() {
        ExerciseRepository exercises = mock(ExerciseRepository.class);
        when(exercises.findById(1L)).thenReturn(Optional.of(Exercise.builder().id(1L)
                .systemPreset(false).user(User.builder().id(2L).build()).build()));
        when(exercises.findById(2L)).thenReturn(Optional.of(Exercise.builder().id(2L).systemPreset(true).build()));
        ExerciseService service = new ExerciseService(exercises, mock(ExerciseTypeRepository.class), mock(UserRepository.class));
        assertDenied(() -> service.getExerciseById(1L));
        for (Long id : List.of(1L, 2L)) assertDenied(() -> service.deleteExercise(id));
        verify(exercises, never()).delete(any());
    }

    private void assertDenied(Runnable action) {
        assertThatThrownBy(action::run).isInstanceOf(AccessDeniedException.class);
    }
}
