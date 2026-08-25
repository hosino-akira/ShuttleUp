package com.shuttleup.backend.service;

import com.shuttleup.backend.dto.response.DashboardResponse;
import com.shuttleup.backend.entity.User;
import com.shuttleup.backend.exception.BadRequestException;
import com.shuttleup.backend.repository.ExerciseRepository;
import com.shuttleup.backend.repository.MatchRepository;
import com.shuttleup.backend.repository.OpponentRepository;
import com.shuttleup.backend.repository.TrainingRecordRepository;
import com.shuttleup.backend.repository.TrainingSessionRepository;
import com.shuttleup.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock UserRepository userRepository;
    @Mock TrainingSessionRepository trainingSessionRepository;
    @Mock MatchRepository matchRepository;
    @Mock TrainingRecordRepository trainingRecordRepository;
    @Mock ExerciseRepository exerciseRepository;
    @Mock OpponentRepository opponentRepository;
    DashboardService service;

    @BeforeEach
    void 準備() {
        service = new DashboardService(userRepository, trainingSessionRepository, matchRepository,
                trainingRecordRepository, exerciseRepository, opponentRepository);
    }

    @Test
    void データがない場合はゼロと空配列を返す() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(trainingSessionRepository.findDashboardRows(1L, null, null)).thenReturn(List.of());
        when(matchRepository.findDashboardScores(1L, null, null, null)).thenReturn(List.of());

        DashboardResponse response = service.getDashboard(1L, null, null, null, null);

        assertThat(response.getSummary().getTrainingSessionCount()).isZero();
        assertThat(response.getSummary().getWinRate()).isZero();
        assertThat(response.getMonthlyTraining()).isEmpty();
        assertThat(response.getExerciseProgress()).isEmpty();
        assertThat(response.getOpponentMatchSummary()).isNull();
    }

    @Test
    void トレーニングと勝敗を集計し中間月を補完する() {
        when(userRepository.existsById(1L)).thenReturn(true);
        var trainingRows = List.of(
                trainingRow(LocalDate.of(2026, 1, 10), 60),
                trainingRow(LocalDate.of(2026, 3, 10), 90));
        var scoreRows = List.of(scoreRow(21, 10), scoreRow(21, 18), scoreRow(15, 21));
        when(trainingSessionRepository.findDashboardRows(1L, null, null)).thenReturn(trainingRows);
        when(matchRepository.findDashboardScores(1L, null, null, null)).thenReturn(scoreRows);

        DashboardResponse response = service.getDashboard(1L, null, null, null, null);

        assertThat(response.getSummary().getTotalDurationMinutes()).isEqualTo(150);
        assertThat(response.getSummary().getWinCount()).isEqualTo(2);
        assertThat(response.getSummary().getLossCount()).isEqualTo(1);
        assertThat(response.getSummary().getWinRate()).isEqualTo(66.7);
        assertThat(response.getMonthlyTraining()).extracting("month")
                .containsExactly("2026-01", "2026-02", "2026-03");
        assertThat(response.getMonthlyTraining().get(1).getSessionCount()).isZero();
    }

    @Test
    void 期間条件をRepositoryへ渡す() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(trainingSessionRepository.findDashboardRows(1L, from, to)).thenReturn(List.of());
        when(matchRepository.findDashboardScores(1L, from, to, null)).thenReturn(List.of());
        service.getDashboard(1L, from, to, null, null);
        org.mockito.Mockito.verify(trainingSessionRepository).findDashboardRows(1L, from, to);
        org.mockito.Mockito.verify(matchRepository).findDashboardScores(1L, from, to, null);
    }

    @Test
    void 開始日が終了日より後の場合は入力エラーになる() {
        assertThatThrownBy(() -> service.getDashboard(1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1), null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("開始日は終了日以前の日付を指定してください。");
    }

    @Test
    void 勝率は小数第1位に丸める() {
        assertThat(DashboardService.calculateWinRate(2, 3)).isEqualTo(66.7);
        assertThat(DashboardService.calculateWinRate(0, 0)).isZero();
    }

    private TrainingSessionRepository.TrainingSummaryRow trainingRow(LocalDate date, int minutes) {
        var row = mock(TrainingSessionRepository.TrainingSummaryRow.class);
        when(row.getTrainingDate()).thenReturn(date);
        when(row.getDurationMinutes()).thenReturn(minutes);
        return row;
    }

    private MatchRepository.MatchScoreRow scoreRow(int myScore, int opponentScore) {
        var row = mock(MatchRepository.MatchScoreRow.class);
        when(row.getMyScore()).thenReturn(myScore);
        when(row.getOpponentScore()).thenReturn(opponentScore);
        return row;
    }
}
