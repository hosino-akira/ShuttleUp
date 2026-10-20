package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.request.*;
import com.shuttleup.backend.entity.*;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InactiveMasterRecordTest {
    private final TrainingRecordRepository records = mock(TrainingRecordRepository.class);
    private final TrainingSessionRepository sessions = mock(TrainingSessionRepository.class);
    private final ExerciseCatalogService catalogs = mock(ExerciseCatalogService.class);
    private final MatchRepository matches = mock(MatchRepository.class);
    private final OpponentRepository opponents = mock(OpponentRepository.class);
    private final TrainingRecordService training = new TrainingRecordService(records, sessions, catalogs);
    private final MatchService matchService = new MatchService(matches, sessions, opponents);
    private final User user = new User();
    private final TrainingSession session = new TrainingSession();
    private final ExerciseCategory category = ExerciseCategory.builder().id(1L).name("Category").enabled(true).build();
    private final ExerciseType type = ExerciseType.builder().id(2L).name("Type").category(category).enabled(true).build();
    private final Exercise inactive = Exercise.builder().id(3L).name("Inactive").exerciseType(type).systemPreset(true).enabled(false).build();
    private final Exercise active = Exercise.builder().id(4L).name("Active").exerciseType(type).systemPreset(true).build();
    private final TrainingRecord record = new TrainingRecord();
    private final Opponent opponent = new Opponent();
    private final Match match = new Match();

    @BeforeEach void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("10", "", List.of()));
        user.setId(10L); session.setId(20L); session.setUser(user);
        record.setId(30L); record.setTrainingSession(session); record.setExercise(inactive);
        opponent.setId(40L); opponent.setUser(user); opponent.setName("Opponent"); opponent.setEnabled(false);
        match.setId(50L); match.setTrainingSession(session); match.setOpponent(opponent);
        when(sessions.findById(20L)).thenReturn(Optional.of(session));
        when(records.findById(30L)).thenReturn(Optional.of(record));
        when(records.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(catalogs.load()).thenReturn(new ExerciseCatalogService.Catalog(
                List.of(category), List.of(type), List.of(inactive, active), List.of()));
        when(opponents.findById(40L)).thenReturn(Optional.of(opponent));
        when(matches.findById(50L)).thenReturn(Optional.of(match));
        when(matches.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void newTrainingCannotUseDisabledExercise() {
        var request = new TrainingRecordCreateRequest(); request.setExerciseId(3L);
        assertThrows(BadRequestException.class, () -> training.createTrainingRecord(20L, request));
        verify(records, never()).save(any());
    }

    @Test void existingTrainingCanRetainDisabledExerciseAndChangeValues() {
        var request = new TrainingRecordUpdateRequest(); request.setExerciseId(3L); request.setSets(5);
        assertEquals(5, training.updateTrainingRecord(30L, request).getSets());
        assertEquals(3L, record.getExercise().getId());
    }

    @Test void changingAnExistingTrainingToDisabledExerciseIsRejected() {
        record.setExercise(active);
        var request = new TrainingRecordUpdateRequest(); request.setExerciseId(3L);
        assertThrows(BadRequestException.class, () -> training.updateTrainingRecord(30L, request));
        assertSame(active, record.getExercise());
    }

    @Test void newMatchCannotUseDisabledOpponent() {
        var request = new MatchCreateRequest(); request.setOpponentId(40L);
        assertThrows(BadRequestException.class, () -> matchService.createMatch(20L, request));
        verify(matches, never()).save(any());
    }

    @Test void existingMatchCanRetainDisabledOpponentAndChangeScores() {
        var request = new MatchUpdateRequest(); request.setOpponentId(40L); request.setMyScore(21); request.setOpponentScore(15);
        assertEquals(21, matchService.updateMatch(50L, request).getMyScore());
        assertSame(opponent, match.getOpponent());
    }

    @Test void differentDisabledOpponentCannotBeAssignedToExistingMatch() {
        var previous = new Opponent(); previous.setId(99L); previous.setUser(user); match.setOpponent(previous);
        var request = new MatchUpdateRequest(); request.setOpponentId(40L);
        assertThrows(BadRequestException.class, () -> matchService.updateMatch(50L, request));
        assertSame(previous, match.getOpponent());
    }

    @Test void historicalTrainingUsesPersonalNamesWithoutChangingRecordValues() {
        var setting = new ExerciseMasterSetting();
        setting.setTargetKind(MasterKind.EXERCISE); setting.setTargetId(3L);
        setting.setName("Renamed exercise"); setting.setEnabled(false);
        record.setSets(7);
        when(catalogs.load()).thenReturn(new ExerciseCatalogService.Catalog(
                List.of(category), List.of(type), List.of(inactive, active), List.of(setting)));
        var response = training.getTrainingRecord(30L);
        assertEquals("Renamed exercise", response.getExerciseName());
        assertEquals(30L, response.getId());
        assertEquals(3L, response.getExerciseId());
        assertEquals(7, response.getSets());
        assertEquals("Inactive", inactive.getName());
    }

    @Test void historicalMatchUsesTheCurrentOpponentName() {
        opponent.setName("Renamed opponent");
        var response = matchService.getMatch(50L);
        assertEquals("Renamed opponent", response.getOpponentName());
        assertEquals(40L, response.getOpponentId());
    }
}
